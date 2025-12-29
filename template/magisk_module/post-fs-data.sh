#!/system/bin/sh
MODDIR=${0%/*}
MODULE_ID=$(basename "$MODDIR")

if [ "$ZYGISK_ENABLED" = false ]; then
  log -p w -t "Sui" "Zygisk is disabled"
  exit 1
fi

if [ "$KSU" = true ]; then
  log -p i -t "Sui" "KernelSU ksud version $KSU_VER ($KSU_VER_CODE)"
  log -p i -t "Sui" "KernelSU kernel version $KSU_KERNEL_VER_CODE"
  apply_sepolicy() {
    ksud sepolicy apply "$1"
  }
elif [  "$KERNELPATCH" = true  ]; then
  kp_major_char=${KERNELPATCH_VERSION:0:1}
  kp_minor_patch=${KERNELPATCH_VERSION:1}

  kp_major=$(( $(printf '%d' "'$major_char") - 97 ))
  kp_minor=$(( 10 + ${minor_patch:0:1} ))
  kp_patch=${minor_patch:1}

  log -p i -t "Sui" "APatch version $APATCH_VER ($APATCH_VER_CODE)"
  log -p i -t "Sui" "KernelPatch version $major.$minor.$patch"
  apply_sepolicy() {
    apd sepolicy apply "$1"
  }
else
  MAGISK_VER_CODE=$(magisk -V)

  log -p i -t "Sui" "Magisk version $MAGISK_VER_CODE"
  apply_sepolicy() {
    magiskpolicy --live --apply "$1"
  }
fi

log -p i -t "Sui" "Module path $MODDIR"

enable_once="/data/adb/sui/enable_adb_root_once"
enable_forever="/data/adb/sui/enable_adb_root"
adb_root_exit=0

if [ -f $enable_once ]; then
  log -p i -t "Sui" "adb root support is enabled for this time of boot"
  rm $enable_once
  enable_adb_root=true
fi

if [ -f $enable_forever ]; then
  log -p i -t "Sui" "adb root support is enabled forever"
  enable_adb_root=true
fi

if [ "$enable_adb_root" = true ]; then
  log -p i -t "Sui" "Setup adb root support"

  # Make sure sepolicy.rule be loaded
  chmod 755 "$MODDIR/sepolicy_checker"
  if ! "$MODDIR/sepolicy_checker"; then
    log -p e -t "Sui" "RootImpl does not load sepolicy.rule..."
    log -p e -t "Sui" "Try to load it..."
    apply_sepolicy "$MODDIR"/sepolicy.rule
    log -p i -t "Sui" "Apply finished"
  else
    log -p i -t "Sui" "RootImpl should have loaded sepolicy.rule correctly"
  fi

  # Setup adb root support
  rm "$MODDIR/bin/adb_root"
  ln -s "$MODDIR/bin/sui" "$MODDIR/bin/adb_root"
  chmod 700 "$MODDIR/bin/adb_root"
  "$MODDIR/bin/adb_root" "$MODDIR"
  adb_root_exit=$?
  log -p i -t "Sui" "Exited with $adb_root_exit"
else
  log -p i -t "Sui" "adb root support is disabled"
fi

# Setup uninstaller
rm "$MODDIR/bin/uninstall"
ln -s "$MODDIR/bin/sui" "$MODDIR/bin/uninstall"

# Run Sui server
chmod 700 "$MODDIR"/bin/sui

# define print_log
print_log() {
    echo "[$(date)] $1" >> "$MODDIR/sui.log"
    log -p i -t "SuiDaemon" "$1"
}


print_log "Starting Sui native daemon..."

# strat the sui daemon
nohup "$MODDIR"/bin/sui "$MODDIR" "$adb_root_exit" >> "$MODDIR/sui.log" 2>&1 &

print_log "Sui daemon launched with PID $!"

exit 0