#!/usr/bin/env python3
"""Seal native render evidence for private review; no usable screenshot is uploaded.

RSA-OAEP-SHA256 wraps a fresh random password; AES-256-CBC/PBKDF2 encrypts
captures and HMAC-SHA256 authenticates the ciphertext with a separate derived
key. The private RSA key is held only by the reviewer, never in the repository.
"""
import argparse, hashlib, hmac, json, os, pathlib, secrets, subprocess, tempfile, zipfile

def seal(root, public_key, output):
    root=pathlib.Path(root);output=pathlib.Path(output);output.parent.mkdir(parents=True,exist_ok=True)
    with tempfile.TemporaryDirectory() as work:
        work=pathlib.Path(work);plain=work/'captures.zip'
        count=0
        with zipfile.ZipFile(plain,'w',compression=zipfile.ZIP_DEFLATED) as z:
            for directory in ['landscape-parity','phone-landscape','phone-refinement','portrait-polish','landscape-baseline','tablet-polish','protected-home','protected-baseline']:
                for p in sorted((root/directory).rglob('*')):
                    if p.is_file() and p.suffix.lower() in ['.png','.txt']:
                        z.write(p,p.relative_to(root));count+=1
        if not count: raise SystemExit('No render captures were produced')
        password=secrets.token_hex(64).encode();(work/'password').write_bytes(password)
        subprocess.run(['openssl','enc','-aes-256-cbc','-pbkdf2','-iter','200000','-salt','-in',str(plain),'-out',str(work/'payload.enc'),'-pass','file:'+str(work/'password')],check=True)
        subprocess.run(['openssl','pkeyutl','-encrypt','-pubin','-inkey',str(public_key),'-in',str(work/'password'),'-out',str(work/'wrapped-key.bin'),'-pkeyopt','rsa_padding_mode:oaep','-pkeyopt','rsa_oaep_md:sha256','-pkeyopt','rsa_mgf1_md:sha256'],check=True)
        auth_key=hashlib.sha256(b'landscape-capture-auth\x00'+password).digest()
        mac=hmac.new(auth_key,digestmod=hashlib.sha256)
        with (work/'payload.enc').open('rb') as f:
            for block in iter(lambda:f.read(1024*1024),b''):mac.update(block)
        (work/'mac.bin').write_bytes(mac.digest())
        (work/'manifest.json').write_text(json.dumps({'schema':'private-render-envelope-v1','source_sha':os.environ.get('GITHUB_SHA'),'theme':os.environ.get('MIQAAT_VISUAL_THEME')}))
        with zipfile.ZipFile(output,'w',compression=zipfile.ZIP_STORED) as z:
            for name in ['payload.enc','wrapped-key.bin','mac.bin','manifest.json']:z.write(work/name,name)
    print('Encrypted private review envelope created; no plaintext capture uploaded.')
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('root');p.add_argument('public_key');p.add_argument('output');a=p.parse_args();seal(a.root,a.public_key,a.output)
