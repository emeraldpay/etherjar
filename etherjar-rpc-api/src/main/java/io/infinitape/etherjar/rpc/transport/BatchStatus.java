package io.infinitape.etherjar.rpc.transport;

import java.util.Objects;

public class BatchStatus {

    private int succeed;
    private int failed;
    private int total;

    public static BatchStatus empty() {
        return BatchStatus.newBuilder()
            .withSucceed(0)
            .withFailed(0)
            .withTotal(0)
            .build();
    }

    public static BatchStatus single() {
        return BatchStatus.newBuilder()
            .withSucceed(1)
            .withFailed(0)
            .withTotal(1)
            .build();
    }

    private BatchStatus(int total) {
        this.total = total;
    }

    public int getSucceed() {
        return succeed;
    }

    public int getFailed() {
        return failed;
    }

    public int getTotal() {
        return total;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchStatus status = (BatchStatus) o;
        return succeed == status.succeed &&
                failed == status.failed &&
                total == status.total;
    }

    @Override
    public int hashCode() {
        return Objects.hash(succeed, failed, total);
    }

    public static class Builder {
        private Integer succeed = null;
        private Integer failed = null;
        private Integer total = null;

        public Builder withTotal(int total) {
            this.total = total;
            return this;
        }

        public Builder withSucceed(int succeed) {
            this.succeed = succeed;
            return this;
        }

        public Builder withFailed(int failed) {
            this.failed = failed;
            return this;
        }

        public BatchStatus build() {
            BatchStatus status = new BatchStatus(total);
            if (this.succeed == null) {
                throw new IllegalStateException("Batch .succeed is not set");
            }
            status.succeed = this.succeed;
            if (this.failed == null) {
                throw new IllegalStateException("Batch .failed is not set");
            }
            status.failed = this.failed;
            return status;
        }


    }
}
