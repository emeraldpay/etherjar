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
    DEB=parity_1.5.0_amd64.deb
    URL=https://smartbrood.com/parity/${DEB}

    if [ ! -f ${PWD}/${DEB} ] ; then
      apt-get update
      apt-get install -y wget

      echo "wget -q ${URL}"
      wget -q ${URL}
      dpkg -i ${DEB}
    fi

    parity --chain dev --jsonrpc-interface all --jsonrpc-hosts all
  SHELL
end
