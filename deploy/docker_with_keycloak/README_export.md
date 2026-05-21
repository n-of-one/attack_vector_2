# Export only the docker configuration to run on a raspberry pi (linux/arm64)

1. Edit the .env file follow the README.md directives
2. Run `sh export.sh` and wait
3. Copy the newly created folder export to the other device
4. Run `bash run.sh` inside the export folder

## Load a new version of the av2 image
`docker load -i av2image.tar`