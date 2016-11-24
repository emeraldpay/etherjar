import org.ethereumclassic.etherjar.model.Address;
import org.ethereumclassic.etherjar.model.EtherUnit;
import org.ethereumclassic.etherjar.rpc.DefaultRpcClient;
import org.ethereumclassic.etherjar.rpc.json.BlockTag;
import org.ethereumclassic.etherjar.rpc.transport.DefaultRpcTransport;

import java.net.URI;

public class SimpleTest {
    public static void main(String[] args) {
        try {
            Runtime.getRuntime().exec("curl -H \"Content-Type: application/json\" -X POST " +
                        "--data \'{\"jsonrpc\":\"2.0\", \"method\":\"parity_newAccountFromPhrase\",\"params\":[\"insecure\",\"hunter2\"],\"id\": 22}\' " +
                        "localhost:8545");

            DefaultRpcClient client= new DefaultRpcClient(new DefaultRpcTransport(new URI("http://localhost:8545")));
            Address[] accounts = client.eth().getAccounts().get();
            String balance = client.eth().getBalance(accounts[0], BlockTag.LATEST)
                .get()
                .toString(EtherUnit.ETHER, 1);
            System.out.println(String.format("Account address:{0} with balance:{1}", accounts[0].toString(), balance));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
