
image_name="tb-app"
image_tag_name="latest"
repo_name="orgtboot/trading-boot"


if [ "$1" ]; then
  image_tag_name=$1
else
  image_tag_name="latest"
fi

docker pull ghcr.io/$repo_name/$image_name:$image_tag_name

docker run -d -p 8087:8087 ghcr.io/$repo_name/$image_name:$image_tag_name
