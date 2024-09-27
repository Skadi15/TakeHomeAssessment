package skadi15;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderSummary {
    private final int numApples;
    private final int numOranges;
    private final float totalCost;
}
