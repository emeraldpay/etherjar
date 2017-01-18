# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  config.vm.define "parity" do |parity|
    parity.vm.box = "ubuntu/trusty64"
  end

  config.vm.provider "virtualbox" do |v|
    v.memory = 2048
  end

  config.vm.network "forwarded_port", guest: 8545, host: 8545

  config.vm.provision "shell", name: "install parity", inline: <<-SHELL
    NAME=parity
    VERSION=1.5.0
    DEB=${NAME}_${VERSION}_amd64.deb
    URL=https://smartbrood.com/${NAME}/${DEB}

    if dpkg-query -W parity 2>/dev/null | grep ${VERSION}; then
      echo "${NAME} already installed."
    else
      echo "wget -q ${URL}"
      wget -q ${URL}
      dpkg -i ${DEB}
      ln -s /vagrant/${NAME} /etc/${NAME}
      cp /vagrant/${NAME}/init /etc/init.d/${NAME}
      update-rc.d ${NAME} defaults
    fi
  SHELL

$script = <<SCRIPT
export CONFIG=${CONFIG-:default} CHAIN=${CHAIN-:default} ; /etc/init.d/parity restart
SCRIPT

  config.vm.provision "shell" do |s|
    s.name = "restart parity"
    s.inline = $script
    s.env = { "CONFIG": ENV['CONFIG'], "CHAIN": ENV['CHAIN']}
  end

end
