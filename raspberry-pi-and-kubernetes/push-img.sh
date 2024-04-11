#!/bin/bash

# Default values
source ../config.sh

if [[ ! -v RPI_OS_XZ_URL || -z $RPI_OS_XZ_URL ]]; then
	RPI_OS_XZ_URL="https://downloads.raspberrypi.com/raspios_lite_arm64/images/raspios_lite_arm64-2024-03-15/2024-03-15-raspios-bookworm-arm64-lite.img.xz"
fi

RPI_OS_XZ="raspios-bookworm-arm64-lite.img.xz"
RPI_OS_IMG="raspios-bookworm-arm64-lite.img"

# Check if image file exists
if [ ! -f $RPI_OS_IMG ]; then

	# Check if the XZ compressed image file is downloaded
	if [ ! -f $RPI_OS_XZ ]; then

		echo "No image or compressed XZ file detected, downloading and decompressing..."
		wget -O $RPI_OS_XZ $RPI_OS_XZ_URL
	
	else

		echo "No image file detected, decompressing..."

	fi
	
	xz -dkc $RPI_OS_XZ > $RPI_OS_IMG

fi

# Choose device file
echo "--------------------BLOCK DEVICES--------------------"
lsblk
echo "--------------------BLOCK DEVICES--------------------"

echo "Choose a device file to push the image to:"
read DEV_FILE

# Unmount all of its partitions
sudo umount $DEV_FILE*

echo "Pushing image..."

# Push image file to SD card
sudo dd if=$RPI_OS_IMG of=$DEV_FILE bs=4M conv=fsync status=progress

echo "Ejecting device..."

# Ejecting device safely
sudo eject $DEV_FILE

