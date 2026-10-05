#!/usr/bin/env python3
"""Black-box UI crawler for the unmodified Miqaat APK. Drives the real app through adb + uiautomator only.
Read-only audit tooling: nothing here touches the app source."""
import argparse, hashlib, json, os, re, subprocess, sys, time
import xml.etree.ElementTree as ET

PKG = "com.usman.miqaat"
ap = argparse.ArgumentParser()
ap.add_argument("--out", required=True)
ap.add_argument("--tag", required=True)
ap.add_argument("--themes", default="none")          # comma list of theme keywords, or none
ap.add_argument("--lang", default="en")
ap.add_argument("--font", default="1.0")
ap.add_argument("--depth", type=int, default=3)
ap.add_argument("--max-actions", type=int, default=160)
ap.add_argument("--budget-min", type=int, default=100)
ap.add_argument("--mode", default="crawl")   # crawl | lessons | azaan | times
A = ap.parse_args()
os.makedirs(A.out, exist_ok=True)
T0 = time.time()
TRACE = open(os.path.join(A.out, "trace.jsonl"), "a")
SKIP = re.compile(r"(delete|erase|reset|clear all|uninstall|restore|import|check for update|download|install|share|send|rate|open in)", re.I)

def sh(*a, timeout=40, raw=False):
    try:
        r = subprocess.run(["adb", *a], capture_output=True, timeout=timeout)
        return r.stdout if raw else r.stdout.decode("utf-8", "replace")
    except Exception as e:
        return b"" if raw else f"ERR {e}"

def log(**k):
    k["t"] = round(time.time() - T0, 1)
    TRACE.write(json.dumps(k, ensure_ascii=False) + "\n"); TRACE.flush()
    print(json.dumps(k, ensure_ascii=False)[:220], flush=True)

def over_budget(): return (time.time() - T0) > A.budget_min * 60

def dump():
    r = dump0()
    for _ in range(3):
        if r and any("isn't responding" in label(n) or "isn’t responding" in label(n) for n in r):
            who = [label(n) for n in r if "responding" in label(n)]
            log(ev="system-anr-dialog", who=who)
            w = [n for n in r if label(n) == "Wait"]
            if w: tap(w[0])
            r = dump0()
        else: break
    return r

def dump0():
    for _ in range(4):
        sh("shell", "rm", "-f", "/sdcard/u.xml")
        sh("shell", "uiautomator", "dump", "/sdcard/u.xml")
        x = sh("shell", "cat", "/sdcard/u.xml")
        if x.startswith("<?xml"):
            try: return parse(x)
            except Exception: pass
        time.sleep(1)
    return None

BR = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")
def parse(x):
    root = ET.fromstring(x); out = []
    for n in root.iter("node"):
        m = BR.match(n.get("bounds", ""))
        if not m: continue
        l, t, r, b = map(int, m.groups())
        out.append(dict(text=n.get("text", ""), desc=n.get("content-desc", ""), click=n.get("clickable") == "true",
                        scroll=n.get("scrollable") == "true", pkg=n.get("package", ""), cls=n.get("class", ""),
                        b=(l, t, r, b), enabled=n.get("enabled") == "true", checked=n.get("checked") == "true",
                        sel=n.get("selected") == "true", foc=n.get("focusable") == "true", chk=n.get("checkable") == "true"))
    return out

def label(n): return (n["text"] or n["desc"]).strip().replace("\n", " ")
def skey(nodes): return hashlib.md5("|".join(sorted({re.sub(r"[0-9٠-٩]+", "#", label(n)) for n in nodes if label(n)})).encode()).hexdigest()[:10]
def actionable(n, W=None, H=None):
    if not (n["enabled"] and label(n)): return False
    if not (n["click"] or n["chk"] or n["foc"]): return False
    return True
def screen_size(nodes):
    return max(n["b"][2] for n in nodes), max(n["b"][3] for n in nodes)

def shot(name):
    p = os.path.join(A.out, name + ".png")
    d = sh("exec-out", "screencap", "-p", raw=True)
    open(p, "wb").write(d); return os.path.basename(p)

def tap(n):
    l, t, r, b = n["b"]; sh("shell", "input", "tap", str((l + r) // 2), str((t + b) // 2)); time.sleep(1.3)
def back(): sh("shell", "input", "keyevent", "4"); time.sleep(1.1)
def swipe(w, h, up=True):
    x = w // 2; y1, y2 = (int(h * .78), int(h * .28)) if up else (int(h * .28), int(h * .78))
    sh("shell", "input", "swipe", str(x), str(y1), str(x), str(y2), "350"); time.sleep(0.9)
def fg():
    o = sh("shell", "dumpsys", "window")
    m = re.search(r"mCurrentFocus=.*?\{[^ ]+ [^ ]+ ([^/ }]+)", o)
    return m.group(1) if m else "?"
def launch():
    sh("shell", "monkey", "-p", PKG, "-c", "android.intent.category.LAUNCHER", "1"); time.sleep(3.5)

PRAYERS = ("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
def in_setup(nodes): return any(re.match(r"Step \d+ of \d+", label(n)) for n in nodes)
def is_home(nodes):
    if in_setup(nodes): return False
    labs = " ".join(label(n) for n in nodes)
    return sum(p in labs for p in PRAYERS) >= 4 or "فجر" in labs and "مغرب" in labs

SETUP_PRIORITY = ["got it", "begin", "these look right", "next", "finish", "done", "continue", "wait"]
UNDER = {"back", "settings", "learn salah", "monthly timetable", "back", "ترتیبات"}
def setup(tag):
    """Walk the first-run gate, recording every screen. Home controls remain exposed under it (audited separately)."""
    for i in range(16):
        nodes = dump()
        if not nodes: time.sleep(2); continue
        if is_home(nodes):
            log(ev="setup-done", step=i); return True
        f = shot(f"{tag}__setup{i}")
        log(ev="setup-screen", step=i, shot=f, key=skey(nodes), labels=[label(n) for n in nodes if label(n)][:60],
            clickables=[label(n) for n in nodes if n["click"] and label(n)])
        labs = [label(n) for n in nodes]
        if "Where will Miqaat be used?" in labs and not getattr(setup, "locdone", False):
            setup.locdone = True
            n = find(nodes, "Use my location")
            if n:
                tap(n); time.sleep(20)
                nn = dump() or []
                f2 = shot(f"{tag}__setup{i}_after-detect-20s")
                log(ev="detect-result", shot=f2, labels=[label(x) for x in nn if label(x)][:40])
                if in_setup(nn) and find(nn, "Sydney, NSW"): 
                    tap(find(nn, "Sydney, NSW")); log(ev="setup-tap", label="Sydney, NSW (preset; detect did not complete)")
                continue
        cl = [n for n in nodes if n["enabled"] and label(n) and label(n).lower() not in UNDER]
        pick = None
        for want in SETUP_PRIORITY:
            for n in cl:
                if label(n).lower() == want: pick = n; break
            if pick: break
        if pick: log(ev="setup-tap", label=label(pick)); tap(pick)
        else:
            log(ev="setup-stuck")
            w, h = screen_size(nodes); swipe(w, h)
            if i > 14: break
        time.sleep(1.5)
    return False

def find(nodes, text, nth=0):
    m = [n for n in nodes if text.lower() in label(n).lower()]
    return m[nth] if len(m) > nth else None

def scroll_find(text, maxs=8):
    """Scroll down until a node containing text is visible; returns node or None."""
    for i in range(maxs + 1):
        nodes = dump()
        if nodes:
            n = find(nodes, text)
            if n and n["b"][3] <= screen_size(nodes)[1] and n["b"][1] >= 0: return n
            w, h = screen_size(nodes)
        else: w, h = 1080, 2000
        if i < maxs: swipe(w, h)
    return None

def go_home():
    for _ in range(4):
        nodes = dump()
        if nodes and is_home(nodes): return True
        if fg() != PKG: launch()
        else: back()
        time.sleep(1)
    launch(); n = dump(); return bool(n and is_home(n))

def reset_app_to_home():
    sh("shell", "am", "force-stop", PKG); launch()
    return go_home()

def nav_settings(sub=None):
    nodes = dump()
    n = find(nodes, "Settings") or find(nodes, "settings") or find(nodes, "ترتیبات")
    if not n: log(ev="nav-fail", what="Settings"); return False
    tap(n)
    if sub:
        s = scroll_find(sub)
        if not s: log(ev="nav-fail", what=sub); return False
        tap(s)
    return True

shotn = [0]
seen = {}
actions = [0]

def screens_of(tag, path, nodes, depth):
    """Capture a screen at all scroll positions; return clickable candidates [(scrollpos, label, nth)]."""
    k = skey(nodes); cands = []; labseen = {}
    w, h = screen_size(nodes)
    pos = 0
    cur = nodes
    while True:
        shotn[0] += 1
        f = shot(f"{tag}__{shotn[0]:03d}__d{depth}__p{pos}")
        log(ev="screen", depth=depth, path=path, key=skey(cur), pos=pos, shot=f,
            labels=[label(n) for n in cur if label(n)][:60],
            clickables=[label(n) for n in cur if actionable(n)][:40],
            tiny=[label(n) for n in cur if n["click"] and (n["b"][2]-n["b"][0] < 44*DENS or n["b"][3]-n["b"][1] < 44*DENS) and label(n)][:20])
        for n in cur:
            if actionable(n) and n["pkg"] == PKG and not n["scroll"]:
                lab = label(n); labseen[lab] = labseen.get(lab, 0) + 1
                if (lab, labseen[lab] - 1) not in [(c[1], c[2]) for c in cands]: cands.append((pos, lab, labseen[lab] - 1))
        text_cands(cur, cands, labseen, pos)
        if pos >= 5: break
        before = skey(cur)
        swipe(w, h)
        nxt = dump()
        if not nxt or skey(nxt) == before: break
        cur = nxt; pos += 1
    for _ in range(pos): swipe(w, h, up=False)
    return cands

ALLOW = re.compile(r"^(Location|Prayer times|Azaan & alerts|Iqamah|Hijri calendar|Display & art|Try it now|Reliability & backup|Privacy|About|Fajr|Maghrib|Dhuhr · .Asr · Isha|The words|The movements|Next|Continue|Start|Begin|Done|Finish|Play.*|Slow.*|Replay.*|Stop.*|Qibla.*|Morning.*|Evening.*|After prayer.*|Friday.*|Jumu.*)$")
def text_cands(cur, cands, labseen, pos):
    for n in cur:
        lab = label(n)
        if lab and n["pkg"] == PKG and ALLOW.match(lab) and not n["scroll"]:
            labseen[lab] = labseen.get(lab, 0) + 1
            if (lab, labseen[lab] - 1) not in [(c[1], c[2]) for c in cands]: cands.append((pos, lab, labseen[lab] - 1))

def tap_label(lab, nth, pos):
    for _ in range(pos): 
        nodes = dump()
        if not nodes: break
        w, h = screen_size(nodes); swipe(w, h)
    nodes = dump()
    if not nodes: return False
    n = find_exact(nodes, lab, nth)
    if not n: return False
    tap(n); return True

def find_exact(nodes, lab, nth):
    m = [n for n in nodes if label(n) == lab and n["pkg"] == PKG]
    return m[nth] if len(m) > nth else None

def replay(path):
    if not reset_app_to_home(): return False
    for (lab, nth, pos) in path:
        if not tap_label(lab, nth, pos): return False
    return True

def explore(tag, path, depth):
    if over_budget() or actions[0] >= A.max_actions: return
    nodes = dump()
    if not nodes: return
    k = skey(nodes)
    if k in seen: return
    seen[k] = True
    cands = screens_of(tag, [p[0] for p in path], nodes, depth)
    if depth >= A.depth: return
    for (pos, lab, nth) in cands:
        if over_budget() or actions[0] >= A.max_actions: return
        if SKIP.search(lab):
            log(ev="skipped", label=lab, reason="destructive-or-external", path=[p[0] for p in path]); continue
        actions[0] += 1
        if not tap_label(lab, nth, pos):
            log(ev="tap-fail", label=lab); continue
        after = dump()
        f = fg()
        if f != PKG:
            sh2 = shot(f"{tag}__{shotn[0]+1:03d}__external"); shotn[0] += 1
            log(ev="external", label=lab, fg=f, shot=sh2, path=[p[0] for p in path]); 
            launch(); 
            if not (dump() and replay(path)): reset_app_to_home()
            continue
        if not after:
            log(ev="no-dump", label=lab); continue
        ak = skey(after)
        if ak == k:
            shotn[0] += 1; f2 = shot(f"{tag}__{shotn[0]:03d}__same__{re.sub(r'[^A-Za-z0-9]+','_',lab)[:24]}")
            log(ev="no-change", label=lab, shot=f2, path=[p[0] for p in path]); continue
        explore(tag, path + [(lab, nth, pos)], depth + 1)
        # return to parent
        if depth + 1 <= A.depth:
            back()
            n2 = dump()
            if not n2 or skey(n2) != k:
                log(ev="back-mismatch", label=lab, path=[p[0] for p in path])
                if not replay(path): log(ev="replay-fail", path=[p[0] for p in path]); return

def metrics(tag):
    cr = sh("shell", "logcat", "-d", "-b", "crash")
    log(ev="crash-buffer", bytes=len(cr), head=cr[:1500])
    open(os.path.join(A.out, f"{tag}__logcat.txt"), "w").write(sh("shell", "logcat", "-d", "-t", "4000", "*:W"))
    g = sh("shell", "dumpsys", "gfxinfo", PKG)
    keep = [l for l in g.splitlines() if re.search(r"Total frames|Janky|percentile|Number (Missed|High|Slow)", l)]
    log(ev="gfx", lines=keep); 
    m = sh("shell", "dumpsys", "meminfo", PKG)
    log(ev="mem", lines=[l for l in m.splitlines() if re.search(r"TOTAL|Java Heap|Native Heap|Graphics", l)][:8])

THEME_LABEL_EN = {"Miqaat": "Miqaat · illuminated", "Kiswah": "Kiswah", "Celestial": "Celestial", "Gallery": "Prayer Gallery"}
THEME_LABEL_UR = {"Miqaat": "میقات", "Kiswah": "کسوہ", "Celestial": "سماوی", "Gallery": "نماز گیلری"}
def L(en, ur): return ur if A.lang == "ur" else en

def tap_text(text, exact=True, maxs=8, startswith=False):
    """Scroll until a node with this label is visible and tap it (works for non-clickable labels: the click propagates)."""
    for i in range(maxs + 1):
        nodes = dump()
        if nodes:
            w, h = screen_size(nodes)
            for n in nodes:
                l = label(n)
                ok = (l == text) if exact else (l.startswith(text) if startswith else text in l)
                if ok and n["pkg"] == PKG and 0 <= n["b"][1] and n["b"][3] <= h and n["b"][2] > n["b"][0]:
                    tap(n); return True
            if i < maxs: swipe(w, h)
    return False

def to_top():
    for _ in range(3):
        nodes = dump()
        if not nodes: return
        w, h = screen_size(nodes); swipe(w, h, up=False)

def pick_theme2(kw):
    if kw == "none": return True
    want = (THEME_LABEL_UR if A.lang == "ur" else THEME_LABEL_EN).get(kw, kw)
    if not go_home(): return False
    if not tap_text(L("Settings", "ترتیبات")): log(ev="nav-fail", what="Settings"); return False
    if not tap_text(L("Display & art", "ڈسپلے اور آرٹ")): log(ev="nav-fail", what="Display & art"); return False
    if not tap_text(want, exact=False, startswith=True): log(ev="theme-not-found", kw=kw); return False
    log(ev="theme-set", kw=kw); time.sleep(1.5)
    return go_home()

def pick_theme(kw):
    if kw == "none": return True
    if not nav_settings(None): return False
    d = scroll_find("Display & art")
    if d: tap(d)
    for i in range(10):
        nodes = dump() or []
        c = [n for n in nodes if actionable(n) and kw.lower() in label(n).lower()]
        if c:
            tap(c[0]); log(ev="theme-set", kw=kw, label=label(c[0])); return True
        if nodes: swipe(*screen_size(nodes))
    log(ev="theme-not-found", kw=kw); return False

def set_lang_urdu():
    if not nav_settings(None): return False
    d = scroll_find("Display & art")
    if d: tap(d)
    n = scroll_find("اردو") or scroll_find("Urdu")
    if not n: log(ev="lang-not-found"); return False
    tap(n); time.sleep(2); log(ev="lang-set"); return True

DENS = 2.0
def main():
    global DENS
    d = sh("shell", "wm", "density"); m = re.search(r"(\d+)\s*$", d.strip().splitlines()[-1] if d.strip() else "")
    DENS = (int(m.group(1)) / 160.0) if m else 2.0
    log(ev="env", density=DENS, size=sh("shell", "wm", "size").strip(), rot=sh("shell", "dumpsys", "window", "displays")[:0])
    sh("shell", "settings", "put", "system", "font_scale", A.font)
    ts = sh("shell", "am", "start", "-W", "-S", "-n", f"{PKG}/.MainActivity")
    m = re.search(r"TotalTime: (\d+)", ts); log(ev="cold-start-ms", ms=int(m.group(1)) if m else None)
    time.sleep(3)
    ok = setup(A.tag)
    log(ev="setup-result", ok=ok)
    if A.lang == "ur":
        go_home()
        if tap_text("Settings") and tap_text("Display & art"):
            if tap_text("اردو", exact=False): log(ev="lang-set"); time.sleep(2)
            else: log(ev="lang-not-found")
        go_home()
    if A.mode != "crawl": return run_mode()
    for kw in A.themes.split(","):
        tag = f"{A.tag}-{re.sub(r'[^A-Za-z0-9]+','',kw)}-{A.lang}-f{A.font}"
        if not pick_theme2(kw): pass
        if not go_home(): log(ev="home-fail", tag=tag)
        seen.clear(); actions[0] = 0
        explore(tag, [], 0)
        log(ev="theme-done", tag=tag, actions=actions[0], screens=shotn[0])
        go_home()
    metrics(A.tag)

def snap(tag, name, n=None):
    shotn[0] += 1
    f = shot(f"{tag}__{shotn[0]:03d}__{name}")
    nodes = n or dump() or []
    log(ev="shot", name=name, shot=f, labels=[label(x) for x in nodes if label(x)][:60])
    return f

def step_through(tag, prefix, maxsteps=45, scroll_every=6):
    """Walk a lesson with its Continue button, one screenshot per step."""
    for i in range(maxsteps):
        nodes = dump()
        if not nodes: break
        snap(tag, f"{prefix}-step{i+1:02d}", nodes)
        if i % scroll_every == 0:
            w, h = screen_size(nodes); swipe(w, h); snap(tag, f"{prefix}-step{i+1:02d}-scrolled"); swipe(w, h, up=False)
        nxt = [n for n in nodes if label(n).startswith(("Continue", "Next", "Finish", "Done")) and n["pkg"] == PKG and n["b"][1] > screen_size(nodes)[1] * 0.5]
        if not nxt: log(ev="lesson-end", prefix=prefix, steps=i + 1, last=[label(x) for x in nodes if label(x)][-8:]); break
        tap(nxt[0]); time.sleep(0.6)

def back_until(text, tries=5):
    for _ in range(tries):
        nodes = dump() or []
        if any(text in label(n) for n in nodes): return True
        back()
    return False

def open_learn():
    go_home()
    return tap_text("Learn Salah", exact=True)

def mode_lessons():
    for kw in A.themes.split(","):
        tag = f"{A.tag}-{kw}-{A.lang}"
        pick_theme2(kw)
        lessons = ["Fajr"] + (["Maghrib", "Dhuhr · ʿAsr · Isha"] if kw == A.themes.split(",")[0] else [])
        for les in lessons:
            if not open_learn(): log(ev="nav-fail", what="Learn"); continue
            snap(tag, "learn-overview")
            if not tap_text(les, exact=False, startswith=True): log(ev="nav-fail", what=les); continue
            time.sleep(1)
            step_through(tag, "lesson-" + re.sub(r"[^A-Za-z]+", "", les)[:10], maxsteps=45 if les == "Fajr" else 40)
        for pg in ("The words", "The movements"):
            if not open_learn(): continue
            if tap_text(pg):
                time.sleep(1)
                if pg == "The movements":
                    n0 = dump() or []; w, h = screen_size(n0) if n0 else (1080, 2400)
                    for k in range(7):
                        snap(tag, f"movements-p{k}"); swipe(w, h)
                else:
                    step_through(tag, "words", maxsteps=14, scroll_every=99)
        go_home()

def tap_after(anchor, button, maxs=6):
    for i in range(maxs + 1):
        nodes = dump() or []
        if not nodes: continue
        w, h = screen_size(nodes)
        a = [n for n in nodes if label(n) == anchor and n["pkg"] == PKG]
        if a:
            ay = a[0]["b"][1]
            b = sorted([n for n in nodes if label(n) == button and n["b"][1] >= ay and n["b"][3] <= h], key=lambda n: n["b"][1])
            if b: tap(b[0]); return True
        swipe(w, h)
    return False

def mode_azaan():
    for kw in A.themes.split(","):
        tag = f"{A.tag}-{kw}"
        pick_theme2(kw)
        for name, anchor, button, waits in (
            ("azaan-full-fajr", "Everything, exactly as at prayer time", "Fajr", [4, 25]),
            ("azaan-recording", "Azaan recording", "Fajr", [4]),
            ("dua-hadith", "Full sequence after azaan", "Start", [5, 60, 70]),
            ("iqamah-short", "Short countdown", "Start", [4, 10, 12]),
            ("quiet-screen", "Quiet screen", "show", [3]),
        ):
            go_home()
            if not (tap_text("Settings") and tap_text("Try it now")): log(ev="nav-fail", what="Try it now"); continue
            if not tap_after(anchor, button): log(ev="nav-fail", what=name); continue
            t0 = time.time()
            for wsec in waits:
                time.sleep(wsec); snap(tag, f"{name}-t{int(time.time()-t0)}")
            sh("shell", "am", "force-stop", PKG); time.sleep(1); launch(); go_home()

# Sydney local -> UTC (AEDT, UTC+11 from 4 Oct 2026)
SCENARIOS = [
  ("predawn-0430",   "100517302026"),   # Tue 6 Oct 04:30 AEDT
  ("morning-0545",   "100518452026"),   # 05:45 (after Fajr, before sunrise)
  ("forenoon-0930",  "100522302026"),   # 09:30
  ("friday-1250",    "100901502026"),   # Fri 9 Oct 12:50 after Dhuhr: Friday
  ("afternoon-1630", "100705302026"),   # Wed 7 Oct 16:30
  ("maghrib-1915",   "100708152026"),   # 19:15
  ("night-2230",     "100711302026"),   # 22:30
]
def mode_times():
    r = sh("root"); log(ev="adb-root", out=r.strip()[:80]); time.sleep(3)
    sh("shell", "settings", "put", "global", "auto_time", "0"); sh("shell", "settings", "put", "global", "auto_time_zone", "0")
    for kw in A.themes.split(","):
        pick_theme2(kw)
        for sc, stamp in SCENARIOS:
            o = sh("shell", "date", "-u", stamp[:8] + stamp[8:] + ".00")
            log(ev="set-time", sc=sc, out=o.strip()[:60])
            sh("shell", "am", "force-stop", PKG); launch(); time.sleep(3); go_home()
            seen.clear(); actions[0] = 0
            A.depth = 1; A.max_actions = 12
            explore(f"{A.tag}-{kw}-{sc}", [], 0)
            go_home()

def run_mode():
    {"lessons": mode_lessons, "azaan": mode_azaan, "times": mode_times}[A.mode]()
    metrics(A.tag)

main()
