SUMMARY = "High Availability monitor built upon LVS, VRRP and service pollers"
DESCRIPTION = "Keepalived is a routing software written in C. The main goal \
of this project is to provide simple and robust facilities for loadbalancing \
and high-availability to Linux system and Linux based infrastructures. \
Loadbalancing framework relies on well-known and widely used Linux Virtual \
Server (IPVS) kernel module providing Layer4 loadbalancing \
"
HOMEPAGE = "http://www.keepalived.org/"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

SRC_URI = "http://www.keepalived.org/software/${BP}.tar.gz \
           file://keepalived.init \
           file://keepalived.default \
           "

SRC_URI[md5sum] = "230106626157aba1d7efa798a66688e3"
SRC_URI[sha256sum] = "d5bdd25530acf60989222fd92fbfd596e06ecc356a820f4c1015708b76a8d4f3"

DEPENDS = "libnfnetlink openssl"

inherit autotools pkgconfig systemd update-rc.d

PACKAGECONFIG ??= "libnl snmp \
    ${@bb.utils.filter('DISTRO_FEATURES', 'systemd', d)} \
"
PACKAGECONFIG[libnl] = "--enable-libnl,--disable-libnl,libnl"
PACKAGECONFIG[snmp] = "--enable-snmp,--disable-snmp,net-snmp"
PACKAGECONFIG[systemd] = "--with-init=systemd --with-systemdsystemunitdir=${systemd_system_unitdir},--with-init=SYSV,systemd"

EXTRA_OECONF = "--disable-libiptc"
EXTRA_OEMAKE = "initdir=${sysconfdir}/init.d"

do_install_append() {
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/keepalived.init ${D}${sysconfdir}/init.d/${BPN}

    install -d ${D}${sysconfdir}/default
    install -m 0755 ${WORKDIR}/keepalived.default ${D}${sysconfdir}/default/${BPN}

    #if [ -f ${D}${sysconfdir}/init.d/${BPN} ]; then
    #    chmod 0755 ${D}${sysconfdir}/init.d/${BPN}
    #    sed -i 's#rc.d/##' ${D}${sysconfdir}/init.d/${BPN}
    #fi

    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -D -m 0644 ${B}/${BPN}/${BPN}.service ${D}${systemd_system_unitdir}/${BPN}.service
    fi

    ln -s KEEPALIVED-MIB.txt ${D}${datadir}/snmp/mibs/KEEPALIVED-MIB

    rm -rf ${D}${sysconfdir}/keepalived/samples
}

FILES_${PN} += "${datadir}/snmp/mibs"

INITSCRIPT_NAME = "keepalived"
INITSCRIPT_PARAMS = "defaults 30"

SYSTEMD_SERVICE_${PN} = "keepalived.service"
SYSTEMD_AUTO_ENABLE ?= "disable"
