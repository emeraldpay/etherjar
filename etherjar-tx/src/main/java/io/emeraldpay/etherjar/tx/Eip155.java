package io.emeraldpay.etherjar.tx;

/**
 * Replay protection EIP-155.
 *
 * v = CHAIN_ID * 2 + 35
 * v = CHAIN_ID * 2 + 36
 *
 * See spec at https://github.com/ethereum/EIPs/blob/master/EIPS/eip-155.md
 */
public class Eip155 {

    public static int toChainId(int v) {
        //for ethereum mainnet
        if (v == 27 || v == 28) {
            return 1;
        }
        int x = 35;
        if (((v - 36) & 1) == 0) {
            x = 36;
        }
        return (v - x) / 2;
    }

    public static int toV(int y, int chainId) {
        // {0,1} is the parity of the y value of the curve point for which r is the x-value in the secp256k1 signing process
        if (y != 0 && y != 1) {
            throw new IllegalArgumentException("Invalid y. Must be {0, 1}. Actual: "+ y);
        }
        return y + chainId * 2 + 35;
    }

}
