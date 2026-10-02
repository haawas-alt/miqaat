"""Original, editable vector artwork rebuilt from the user-supplied approved Fajr photograph.
No paths from the superseded Claude/Codex figures are reused. Generates Android vectors and review SVGs.
The blank face, long robe and fine charcoal contours follow that approval; this is not scholarly sign-off.
"""
from pathlib import Path
from xml.sax.saxutils import escape
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'app/src/main/res/drawable'
SVG=ROOT/'docs/salah-art'
OUT.mkdir(parents=True,exist_ok=True); SVG.mkdir(parents=True,exist_ok=True)
INK='#39443F'; PAPER='#F9F6EE'; SHADOW='#DDD7CA'
def path(d,fill='none',stroke=INK,width=2.3):return dict(d=d,fill=fill,stroke=stroke,width=width)
def oval(cx,cy,rx,ry,fill=PAPER):
    return path(f'M {cx-rx},{cy} A {rx},{ry} 0 1,0 {cx+rx},{cy} A {rx},{ry} 0 1,0 {cx-rx},{cy} Z',fill)
def write(name,items,w=320,h=460):
    xml=[f'<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="{w}dp" android:height="{h}dp" android:viewportWidth="{w}" android:viewportHeight="{h}">']
    svg=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}">']
    for p in items:
        attrs=f'android:pathData="{escape(p["d"])}" android:fillColor="{p["fill"] if p["fill"]!="none" else "#00000000"}"'
        if p['stroke']!='none':attrs+=f' android:strokeColor="{p["stroke"]}" android:strokeWidth="{p["width"]}" android:strokeLineCap="round" android:strokeLineJoin="round"'
        xml.append('    <path '+attrs+' />')
        svg.append(f'<path d="{p["d"]}" fill="{p["fill"]}" stroke="{p["stroke"]}" stroke-width="{p["width"]}" stroke-linecap="round" stroke-linejoin="round"/>')
    xml.append('</vector>'); svg.append('</svg>')
    (OUT/(name+'.xml')).write_text('\n'.join(xml)+'\n')
    (SVG/(name+'.svg')).write_text('\n'.join(svg)+'\n')
# Frontal standing figure: head approximately one seventh of total body height, long loose robe.
feet=[path('M 126,404 L 126,420 Q 123,428 109,432 Q 106,437 120,437 L 137,432 Q 143,429 142,420 L 144,405 Z',PAPER),path('M 177,405 L 179,421 Q 177,429 184,432 L 201,437 Q 214,438 211,432 Q 197,428 195,420 L 194,404 Z',PAPER)]
neck=path('M 148,99 L 148,115 Q 151,126 161,127 Q 171,126 174,115 L 174,99 Z',PAPER)
robe=path('M 148,115 Q 130,120 112,127 Q 107,139 108,166 L 105,257 L 98,405 Q 159,416 222,405 L 215,257 L 213,166 Q 214,140 207,127 Q 190,120 174,115 Q 172,126 161,128 Q 150,126 148,115 Z',PAPER)
seams=[path('M 161,130 L 161,166',width=1.6),path('M 116,201 Q 116,295 108,392 M 205,203 Q 205,291 213,392',stroke=SHADOW,width=1.1)]
head=oval(161,72,28,34)
# Raised hands: individually drawn fingers and opposed thumbs, palms toward viewer.
leftHand=path('M 96,146 L 91,135 L 91,116 Q 89,111 91,108 L 94,89 Q 96,85 98,90 L 96,109 L 100,84 Q 102,80 104,85 L 101,108 L 107,83 Q 110,81 111,87 L 108,110 L 114,90 Q 117,88 118,94 L 114,116 L 120,110 Q 125,108 126,113 Q 125,118 119,126 L 113,137 L 111,149 Z',PAPER,width=2)
rightHand=path('M 225,146 L 230,135 L 230,116 Q 232,111 230,108 L 227,89 Q 225,85 223,90 L 225,109 L 221,84 Q 219,80 217,85 L 220,108 L 214,83 Q 211,81 210,87 L 213,110 L 207,90 Q 204,88 203,94 L 207,116 L 201,110 Q 196,108 195,113 Q 196,118 202,126 L 208,137 L 210,149 Z',PAPER,width=2)
sleevesTak=[path('M 113,128 Q 108,145 110,165 L 108,174 Q 99,179 93,163 L 88,146 Q 100,140 113,146 L 119,161 Q 119,172 123,175 L 128,144',PAPER),path('M 207,128 Q 213,145 211,165 L 213,174 Q 222,179 228,163 L 233,146 Q 221,140 208,146 L 202,161 Q 202,172 198,175 L 193,144',PAPER)]
write('learn_pose_takbir',feet+[robe,neck,head]+sleevesTak+[leftHand,rightHand]+seams)
# Folded: left wrist below right palm; enough finger definition to show the overlap.
sleevesFold=[path('M 113,128 Q 100,149 99,186 Q 99,200 111,204 L 155,190 L 149,176 L 119,184 L 125,153',PAPER),path('M 207,128 Q 219,151 222,187 Q 222,199 210,204 L 164,191 L 168,176 L 202,184 L 196,153',PAPER)]
foldHands=[path('M 150,177 L 178,176 Q 183,178 180,182 L 157,188 L 147,190 Z',PAPER,width=1.7),path('M 169,177 Q 162,172 155,174 L 141,180 Q 138,183 142,185 L 150,182 L 161,183 Q 168,187 173,191 L 181,179 Z',PAPER,width=1.7),path('M 150,179 L 161,177 M 150,182 L 162,180 M 155,184 L 165,182',width=1.1)]
write('learn_pose_standing',feet+[robe,neck,head]+sleevesFold+foldHands+seams)
sleevesDown=[path('M 112,129 Q 101,148 101,180 L 96,254 Q 104,259 115,256 L 124,169',PAPER),path('M 208,129 Q 220,148 220,180 L 226,254 Q 218,259 207,256 L 198,169',PAPER)]
handsDown=[path('M 98,256 L 95,273 Q 95,279 99,280 L 100,269 L 100,284 Q 103,288 105,282 L 106,270 L 106,284 Q 109,287 111,282 L 112,269 L 112,280 Q 115,281 117,274 L 116,259 Z',PAPER,width=1.7),path('M 224,256 L 227,273 Q 227,279 223,280 L 222,269 L 222,284 Q 219,288 217,282 L 216,270 L 216,284 Q 213,287 211,282 L 210,269 L 210,280 Q 207,281 205,274 L 206,259 Z',PAPER,width=1.7)]
write('learn_pose_rising',feet+[robe,neck,head]+sleevesDown+handsDown+seams)
# Side view, facing left: straight back, shoulders and hips level, hands cup knees.
ruku=[path('M 240,369 L 239,417 Q 237,427 225,433 Q 219,439 232,439 L 258,435 Q 263,434 263,426 L 260,369 Z',PAPER),path('M 214,366 L 213,414 Q 211,424 199,430 Q 194,435 206,436 L 229,432 Q 233,431 233,423 L 235,366 Z',PAPER),path('M 91,169 Q 147,158 233,170 Q 247,175 250,189 L 270,371 Q 248,382 210,372 L 204,290 Q 158,285 116,252 L 94,218 Q 83,191 91,169 Z',PAPER),path('M 83,175 L 99,178 L 104,194 L 87,206 L 77,190 Z',PAPER),oval(65,183,29,26),path('M 107,177 Q 98,185 105,206 L 144,246 L 198,282 L 208,269 L 159,231 L 132,187',PAPER),path('M 204,269 Q 213,269 222,280 L 232,302 Q 232,308 228,304 L 218,287 L 225,308 Q 224,313 220,307 L 210,288 L 215,308 Q 212,312 209,306 L 201,288 Q 194,280 198,276 Z',PAPER,width=1.7),path('M 225,196 L 239,353 M 120,217 Q 159,267 202,272',stroke=SHADOW,width=1.2)]
write('learn_pose_bowing',ruku)
# Sujud: elevated hips and forearms, compact palms beside head, forehead/nose/knees/toes meet mat.
sujud=[path('M 257,358 Q 273,361 287,368 L 297,384 Q 297,391 290,390 L 275,390 L 279,381 L 251,379 Z',PAPER),path('M 123,268 Q 143,222 200,207 Q 215,205 226,226 L 252,352 Q 252,377 232,385 L 166,383 Q 151,378 140,363 L 94,323 Q 98,293 123,268 Z',PAPER),path('M 89,322 L 102,325 L 108,339 L 92,351 L 78,340 Z',PAPER),path('M 88,334 C 76,316 55,319 43,335 C 32,350 32,365 42,379 L 40,386 L 43,391 L 51,391 L 55,384 L 60,389 L 65,388 L 63,380 Q 86,367 89,347 Q 91,341 88,334 Z',PAPER),path('M 128,275 Q 114,278 117,300 L 141,330 L 99,370 L 108,382 L 158,340 Q 164,334 159,324 L 142,289',PAPER),path('M 103,370 L 111,378 Q 108,382 104,385 L 113,386 Q 120,391 111,392 L 83,392 Q 77,390 84,386 L 94,381 Z',PAPER,width=1.8),path('M 87,389 L 105,389 M 90,386 L 104,387',width=1),path('M 234,383 Q 245,380 249,369 M 273,389 L 290,389 M 280,385 L 292,385',width=1.2),path('M 168,256 Q 195,276 219,346 M 171,372 L 222,371',stroke=SHADOW,width=1.2)]
write('learn_pose_prostrating',sujud)
# Sitting profile: upright torso, both hands resting on thighs, folded left foot and upright right foot.
sitBody=[path('M 189,375 Q 162,388 125,391 Q 115,393 114,400 Q 116,405 132,406 L 206,399 L 214,382 Z',PAPER),path('M 233,366 L 241,369 L 245,390 Q 246,399 242,402 L 223,402 Q 215,401 221,396 L 233,392 L 229,378 Z',PAPER),path('M 134,199 Q 120,205 117,223 L 117,304 Q 121,325 139,336 L 217,350 Q 235,357 234,374 Q 232,388 211,392 L 126,379 Q 104,374 103,355 L 109,281 L 108,229 Q 108,208 119,201 Z',PAPER),path('M 119,183 L 118,200 Q 128,211 142,207 L 143,183 Z',PAPER),oval(129,157,26,33),path('M 137,214 Q 150,233 153,273 L 171,319 L 159,329 L 133,279 Q 126,258 124,242',PAPER),path('M 161,318 Q 170,320 185,330 L 201,335 Q 205,339 201,341 L 184,338 L 169,333 L 161,329 Z',PAPER,width=1.8),path('M 181,332 L 197,336 M 177,335 L 192,340',width=1),path('M 126,293 Q 136,314 146,321 M 145,362 L 214,376',stroke=SHADOW,width=1.2)]
write('learn_pose_sitting',sitBody)
# Frontal seated: anatomical RIGHT hand is on viewer LEFT. Raised index separate from folded fingers.
seatFront=[path('M 110,375 L 103,383 L 102,398 Q 102,406 114,406 L 132,403 Q 140,401 134,397 L 116,395 L 119,381 Z',PAPER),path('M 204,382 Q 213,392 229,398 Q 233,404 221,406 L 182,401 L 176,389 Z',PAPER),path('M 146,195 Q 126,198 112,209 Q 103,234 111,281 L 110,321 Q 85,328 81,350 Q 77,368 96,384 Q 124,401 161,395 Q 197,401 225,384 Q 244,368 239,350 Q 235,328 210,321 L 212,281 Q 220,234 208,209 Q 191,198 176,195 Z',PAPER),path('M 148,176 L 147,195 Q 149,207 161,209 Q 174,207 176,195 L 175,176 Z',PAPER),oval(161,147,28,34),path('M 113,211 Q 100,238 103,270 L 100,313 Q 109,324 122,318 L 129,265 L 131,224',PAPER),path('M 209,211 Q 222,238 219,270 L 222,313 Q 213,324 200,318 L 193,265 L 191,224',PAPER),path('M 202,314 L 199,326 L 204,337 Q 211,342 219,341 L 231,338 Q 235,333 228,334 L 217,335 L 208,324 L 220,317 Z',PAPER,width=1.8),path('M 111,313 Q 104,312 100,318 L 95,325 L 79,311 Q 74,309 75,314 L 96,333 Q 101,344 111,342 L 120,338 Q 124,334 120,330 L 114,326 L 121,319 Z',PAPER,width=1.8),path('M 101,331 Q 107,332 110,329 M 105,336 Q 111,338 115,334',width=1.1),path('M 161,212 L 161,245 M 161,345 L 161,381 M 149,297 Q 147,326 140,341 M 169,298 Q 173,326 181,341 M 97,362 Q 124,369 143,375 M 178,375 Q 202,370 226,362',stroke=SHADOW,width=1.2)]
write('learn_pose_tashahhud',seatFront)
# Salam is TWO views; only head/neck turn, seated body and resting hands remain stable.
for side in ('right','left'):
    base=seatFront[:3]+[path('M 148,176 L 147,195 Q 149,207 161,209 Q 174,207 176,195 L 175,176 Z',PAPER)]+seatFront[5:7]
    # Remove finger gesture: relaxed right palm, mirroring the left hand resting on thigh.
    base += [path('M 111,314 L 124,318 L 122,330 L 113,338 L 95,341 Q 86,341 91,336 L 107,331 Z',PAPER,width=1.8),seatFront[7]]
    if side=='left':face=path('M 175,116 C 157,108 139,117 137,135 Q 133,150 142,165 Q 151,179 164,179 L 175,168 L 177,158 L 181,154 Q 181,151 175,146 L 178,140 Q 182,124 175,116 Z',PAPER)
    else:face=path('M 147,116 C 165,108 183,117 185,135 Q 189,150 180,165 Q 171,179 158,179 L 147,168 L 145,158 L 141,154 Q 141,151 147,146 L 144,140 Q 140,124 147,116 Z',PAPER)
    base+=[face,seatFront[-1]]
    write('learn_pose_salam' if side=='right' else 'learn_pose_salam_left',base)
# Architecture behind the figure. Gallery follows the cream arch, sun, low rolling terrain and olive sprig.
for theme,wall,sky,far,mid,near,sun,line in [
('gallery','#E9E3D8','#F4EBDD','#D6AE93','#C6B191','#78866A','#E8C575','#D5CCBC'),
('celestial','#102C4B','#173D59','#566E83','#3B6073','#23485C','#E5BC75','#61778B'),
('miqaat','#18223F','#293759','#777788','#586676','#3E505D','#E2C178','#657188'),
('kiswah','#201F1B','#302D26','#655C47','#514C3D','#3C4033','#D8BC73','#8B7C55')]:
    s=[path('M 0,0 H 600 V 620 H 0 Z',wall,'none'),path('M 45,505 V 225 C 45,110 145,28 277,0 C 408,28 507,110 507,225 V 505 Z',sky,'none'),oval(370,376,26,26,sun)]
    s[-1]['stroke']='none'
    s += [path('M 45,410 Q 158,328 263,388 Q 382,445 507,386 V 507 H 45 Z',far,'none'),path('M 45,442 Q 165,387 267,435 Q 390,492 507,421 V 507 H 45 Z',mid,'none'),path('M 45,466 Q 156,405 261,451 Q 368,494 507,456 V 507 H 45 Z',near,'none'),path('M 45,507 V 225 C 45,110 145,28 277,0 C 408,28 507,110 507,225 V 507',stroke=line,width=2),path('M 70,505 V 225 C 70,123 155,50 277,22 C 397,50 482,123 482,225 V 505',stroke=line,width=1),path('M 0,544 L 95,507 H 463 L 600,558 V 620 H 0 Z',wall,'none'),path('M 89,574 L 145,531 H 414 L 478,574 Z',fill=mid,stroke=line,width=1.2),path('M 115,566 L 151,537 H 409 L 451,566 Z',stroke=line,width=1.1)]
    if theme=='gallery':
        s += [path('M 551,457 Q 559,381 531,302',stroke='#63704E',width=1.6)]
        for d in ['M 551,425 Q 523,409 516,389 Q 541,397 551,425 Z','M 553,403 Q 577,385 577,367 Q 559,380 553,403 Z','M 549,379 Q 520,362 520,346 Q 541,356 549,379 Z','M 542,354 Q 566,336 561,319 Q 545,334 542,354 Z','M 533,329 Q 513,313 515,297 Q 532,312 533,329 Z']:
            s.append(path(d,fill='#73805A',stroke='none'))
    elif theme=='celestial':
        s += [path('M 93,252 A 174,174 0 0,1 443,252',stroke='#B8A172',width=1),path('M 134,125 l 2,5 l 5,2 l -5,2 l -2,5 l -2,-5 l -5,-2 l 5,-2 Z',fill='#E9DCB7',stroke='none'),oval(395,164,2,2,'#E9DCB7')];s[-1]['stroke']='none'
    elif theme=='kiswah':
        s += [path('M 15,40 H 585 M 15,53 H 585 M 15,76 H 585 M 15,89 H 585',stroke='#988663',width=1),path('M 22,53 L 34,65 L 22,76 L 10,65 Z M 578,53 L 590,65 L 578,76 L 566,65 Z',stroke='#988663',width=1)]
    else:s += [path('M 30,52 L 45,68 L 30,84 L 15,68 Z M 553,52 L 568,68 L 553,84 L 538,68 Z',stroke='#8990A0',width=1)]
    write('learn_scene_'+theme,s,600,620)
print('Generated nine posture views and four authored scene vectors.')
