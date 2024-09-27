package skadi15;

import java.io.IOException;

import com.google.common.base.Preconditions;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AppServlet extends HttpServlet {
    private static final String APPLES_PARAMETER = "apples";
    private static final String ORANGES_PARAMETER = "oranges";
    private static final float APPLE_COST = 0.25f;
    private static final float ORANGE_COST = 0.6f;

    @Override
    public void doPost(final HttpServletRequest req, final HttpServletResponse resp) {
        final int numApples = checkParameter(req, APPLES_PARAMETER);
        final int numOranges = checkParameter(req, ORANGES_PARAMETER);

        try {
            resp.getOutputStream().print(processOrder(numApples, numOranges).toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private OrderSummary processOrder(final int numApples, final int numOranges) {
        final float appleCost = numApples * APPLE_COST;
        final float orangeCost = numOranges * ORANGE_COST;
        return OrderSummary.builder()
                .numApples(numApples)
                .numOranges(numOranges)
                .totalCost(appleCost + orangeCost)
                .build();
    }

    private int checkParameter(final HttpServletRequest request, final String parameter) {
        Preconditions.checkArgument(request.getParameterMap().containsKey(parameter), String.format("Parameter %s not in request", parameter));

        final String rawValue = request.getParameter(parameter);
        try {
            final int value = Integer.parseInt(rawValue);
            Preconditions.checkArgument(value >= 0, String.format("Parameter %s must not be negative [value=%d]", parameter, value));
            return value;
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Parameter %s does not contain a valid integer [value=%s]", parameter, rawValue));
        }
    }
}
