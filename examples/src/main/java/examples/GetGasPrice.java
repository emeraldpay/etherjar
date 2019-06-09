package examples;

import io.infinitape.etherjar.domain.Wei;
import io.infinitape.etherjar.rpc.Commands;
import io.infinitape.etherjar.rpc.DefaultRpcClient;
import io.infinitape.etherjar.rpc.RpcClient;
import io.infinitape.etherjar.rpc.transport.DefaultRpcTransport;
import io.infinitape.etherjar.rpc.transport.RpcTransport;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class GetGasPrice {

    public static void main(String[] args)
            throws URISyntaxException, IOException, ExecutionException, InterruptedException {

        try (RpcTransport trans = new DefaultRpcTransport(new URI("http://127.0.0.1:8545"))) {
            RpcClient client = new DefaultRpcClient(trans);
            Future<Wei> req = client.execute(Commands.eth().getGasPrice());

            System.out.println(String.format("Gas Price: %s Ether", req.get().toEthers(12)));
        }
    }
}
