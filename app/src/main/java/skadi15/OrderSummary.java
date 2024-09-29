package skadi15;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class OrderSummary {
    private final UUID orderId;
    private final int numApples;
    private final int numOranges;
    private final float totalCost;

    public boolean isEquivalentTo(final OrderSummary other) {
        if (other == null) {
            return false;
        }

        return numApples == other.numApples
                && numOranges == other.numOranges
                && totalCost == other.totalCost;
    }
}
