/*
 * This file is part of Bitsquare.
 *
 * Bitsquare is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bitsquare is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bitsquare. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bitsquare.gui.util.validation;

import io.bitsquare.gui.util.BSFormatter;
import io.bitsquare.locale.BSResources;
import io.bitsquare.payment.PaymentMethod;
import org.bitcoinj.core.Coin;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.math.BigDecimal;

public class BtcValidator extends NumberValidator {

    private final BSFormatter formatter;

    @NotNull
    private PaymentMethod paymentMethod;

    @Inject
    public BtcValidator(BSFormatter formatter) {
        this.formatter = formatter;
    }

    public void setPaymentMethod(@NotNull PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Override
    public ValidationResult validate(String input) {
        ValidationResult result = validateIfNotEmpty(input);
        if (result.isValid) {
            input = cleanInput(input);
            result = validateIfNumber(input);
        }

        if (result.isValid) {
            result = validateIfNotZero(input)
                    .and(validateIfNotNegative(input))
                    .and(validateIfNotFractionalBtcValue(input))
                    .and(validateIfNotExceedsMaxBtcValue(input));
        }

        return result;
    }

    protected ValidationResult validateIfNotFractionalBtcValue(String input) {
        BigDecimal bd = new BigDecimal(input);
        final BigDecimal satoshis = bd.movePointRight(8);
        if (satoshis.scale() > 0)
            return new ValidationResult(false, BSResources.get("validation.btc.toSmall"));
        else
            return new ValidationResult(true);
    }

    protected ValidationResult validateIfNotExceedsMaxBtcValue(String input) {
        if (Coin.parseCoin(input).compareTo(paymentMethod.getMaxTradeLimitInBitcoin()) > 0)
            return new ValidationResult(false, BSResources.get("validation.btc.toLarge", formatter.formatCoinWithCode(paymentMethod.getMaxTradeLimitInBitcoin())));
        else
            return new ValidationResult(true);
    }
}
