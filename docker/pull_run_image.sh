#!/bin/bash

image_name="tb-app"
image_tag_name="latest"
repo_name="orgtboot/trading-boot"

if [ "$1" ]; then
  image_tag_name=$1
else
  image_tag_name="latest"
fi

echo "[INFO] Login into GitHub Container Repository"
echo "$CR_PAT" | docker login ghcr.io -u "$GH_USER" --password-stdin

echo "[INFO] Download $image_name:$image_tag_name image"
docker pull ghcr.io/$repo_name/$image_name:$image_tag_name

# shellcheck disable=SC2046
if [ $(docker ps -a -q --filter ancestor=ghcr.io/$repo_name/$image_name:$image_tag_name --format="{{.ID}}") ]; then
  echo "[WARN] Container for $image_name:$image_tag_name already running, stopping now..."
  docker rm --force $(docker ps -a -q --filter ancestor=ghcr.io/$repo_name/$image_name:$image_tag_name --format="{{.ID}}")
fi

echo "[INFO] Starting $image_name:$image_tag_name"
docker run --rm -v "${PWD}"/logs:/logs -d -p 8087:8087 ghcr.io/$repo_name/$image_name:$image_tag_name
