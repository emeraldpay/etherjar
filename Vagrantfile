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

  config.vm.provision "shell", inline: <<-SHELL
    VERSION=1.5.0
    DEB=parity_${VERSION}_amd64.deb
    URL=https://smartbrood.com/parity/${DEB}

    if [ ! `dpkg-query -W parity 2>/dev/null | grep ${VERSION}` ] ; then
      echo "wget -q ${URL}"
      wget -q ${URL}
      dpkg -i ${DEB}
    fi
  SHELL

  config.vm.provision "shell", privileged: false, run: 'always', inline: <<-SHELL
    nohup parity --config /vagrant/config.toml &
    sleep 2
  SHELL
end
