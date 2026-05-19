#!/bin/bash

# Check cloudflared installation and print next steps for Cloudflare Tunnel

if command -v cloudflared >/dev/null 2>&1; then
  echo "cloudflared is installed: $(command -v cloudflared)"
  echo
  echo "Next steps (requires Cloudflare account + domain):"
  echo "1) Login to Cloudflare and run: cloudflared login"
  echo "2) Create a tunnel: cloudflared tunnel create mytunnel"
  echo "3) (Optional) Route DNS: cloudflared tunnel route dns mytunnel tunnel-subdomain.yourdomain.com"
  echo "4) Run the tunnel forwarding TCP: cloudflared tunnel --url tcp://localhost:7070"
  echo
  echo "See CLOUD_FLARE_GUIDE.md for details and notes about account/domain requirements."
else
  echo "cloudflared is NOT installed. Install with Homebrew (macOS):"
  echo "  brew install cloudflare/cloudflare/cloudflared"
  echo "Or see https://developers.cloudflare.com/cloudflare-one/connections/connect-apps/install-and-setup/ for other platforms."
fi
