#!/bin/sh

# Fix mount propagation for Cilium (BPF, cgroupv2, netns, etc.)
# Make all mounts shared recursively to support Cilium's eBPF requirements
mount --make-rshared /

# Do not DNAT DNS traffic to Docker's embedded resolver (127.0.0.11).
# That resolver is loopback-only and its replies do not survive Cilium's
# eBPF datapath, which breaks external DNS resolution from pods.
# Kubelet already replaces Docker's loopback resolver with a public resolver
# when generating /etc/resolv.conf for CoreDNS.

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

/bin/k3s server --disable=traefik --flannel-backend=none --disable-network-policy --disable-kube-proxy --kubelet-arg=registry-qps=20 --kubelet-arg=registry-burst=40
