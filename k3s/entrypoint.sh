#!/bin/sh

# Part one: forward requests to docker DNS
# https://github.com/corneliusludmann/k3s-docker-compose-dns/blob/master/entrypoint.sh
# Add IP tables rules to access Docker's internal DNS 127.0.0.11 from outside
# based on https://serverfault.com/a/826424

TCP_DNS_ADDR=$(iptables-save | grep DOCKER_OUTPUT | grep tcp | grep -o '127\.0\.0\.11:.*$')
UDP_DNS_ADDR=$(iptables-save | grep DOCKER_OUTPUT | grep udp | grep -o '127\.0\.0\.11:.*$')

iptables -t nat -A PREROUTING -p tcp --dport 53 -j DNAT --to "$TCP_DNS_ADDR"
iptables -t nat -A PREROUTING -p udp --dport 53 -j DNAT --to "$UDP_DNS_ADDR"


# Add this IP to resolv.conf since CoreDNS of k3s uses this file

TMP_FILE=$(mktemp)
sed "/nameserver.*/ a nameserver $(hostname -i | cut -f1 -d' ')" /etc/resolv.conf > "$TMP_FILE"
cp "$TMP_FILE" /etc/resolv.conf
rm "$TMP_FILE"


# Part two: fix cgroups problems
# https://github.com/k3s-io/k3s/issues/5123#issuecomment-1039674532
# https://github.com/k3d-io/k3d/blob/main/pkg/types/fixes/assets/k3d-entrypoint-cgroupv2.sh

if [ -f /sys/fs/cgroup/cgroup.controllers ]; then
  echo "[$(date -Iseconds)] [CgroupV2 Fix] Evacuating Root Cgroup ..."
  # move the processes from the root group to the /init group,
  # otherwise writing subtree_control fails with EBUSY.
  mkdir -p /sys/fs/cgroup/init
  # new k3s releases only have xargs from findutils
  if command -v xargs >/dev/null; then
    xargs -rn1 </sys/fs/cgroup/cgroup.procs >/sys/fs/cgroup/init/cgroup.procs || :
  else
    busybox xargs -rn1 </sys/fs/cgroup/cgroup.procs >/sys/fs/cgroup/init/cgroup.procs || :
  fi
  # enable controllers
  sed -e 's/ / +/g' -e 's/^/+/' <"/sys/fs/cgroup/cgroup.controllers" >"/sys/fs/cgroup/cgroup.subtree_control"
  echo "[$(date -Iseconds)] [CgroupV2 Fix] Done"
fi

/bin/k3s server --disable=traefik
