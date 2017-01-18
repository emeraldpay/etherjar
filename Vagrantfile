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
      ln -s /vagrant/${NAME}/etc/${NAME} /etc/${NAME}
      cp /vagrant/${NAME}/etc/init.d/${NAME} /etc/init.d/${NAME}
      update-rc.d ${NAME} defaults
    fi
  SHELL

  config.vm.provision "shell", name: "restart service", run: 'always', inline: <<-SHELL
    service parity restart
  SHELL
end
