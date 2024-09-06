#!/bin/bash
# Install to: /opt/dse/explorer/run.sh

BASE_DIR="/opt/dse/explorer"

# X11 Display on localhost
export DISPLAY=:0

# JavaFX
export PATH_TO_FX=/usr/share/openjfx/lib


function print_osd() {
  # This will show for 5 seconds
  DELAY="${2:-5}"
  COLOR="${3:-white}"
  echo "DSE Explorer - $1" | osd_cat -p middle -A center -c "${COLOR}" -d "${DELAY}"
}

function set_hostname() {
      echo "${1}" > /etc/hostname
      sed -i 's/127.0.1.1.*/127.0.1.1\t'"${1}"'/' /etc/hosts
      hostnamectl set-hostname "${1}" --static
}


function get_ip() {
  value=$(ip a show | grep "scope" | grep -Po '(?<=inet )[\d.]+' | grep -v 127.0.0.1 | tail -1)
  echo "$value"
}


function get_serial() {
    if [ -f "/sys/firmware/devicetree/base/serial-number" ] ; then
      SERIAL=$(tr -d '\0' < /sys/firmware/devicetree/base/serial-number)
      set_hostname "monitor-${SERIAL: -8}"
    else
      SERIAL=$(hostname -f)
    fi
    echo "${SERIAL: -8}"    # Only 8 last chars
}


# Get serial and use as hostname and as ID in discovery services
SERIAL=$(get_serial)
export SERIAL

# Loop until we have an IP address
#sleep_seconds=5
#DEVICE_IP=$(get_ip)
#while [ -z "${DEVICE_IP}" ]; do
#  print_osd "Waiting For Network ..."
#  DEVICE_IP=$(get_ip)
#  sleep $sleep_seconds
#done
#export DEVICE_IP

#print_osd "Loading Application" "yellow" "10" &

JAVA_OPTS="-XX:+ExitOnOutOfMemoryError"

exec 2>&1
exec java $JAVA_OPTS -jar "${BASE_DIR}/explorer.jar"
