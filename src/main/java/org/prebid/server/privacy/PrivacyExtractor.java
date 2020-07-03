package org.prebid.server.privacy;

import com.iab.openrtb.request.Regs;
import com.iab.openrtb.request.User;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.ObjectUtils;
import org.prebid.server.exception.PreBidException;
import org.prebid.server.privacy.ccpa.Ccpa;
import org.prebid.server.privacy.model.Privacy;
import org.prebid.server.proto.openrtb.ext.request.ExtRegs;
import org.prebid.server.proto.openrtb.ext.request.ExtUser;

import java.util.List;

/**
 * GDPR-aware utilities
 */
public class PrivacyExtractor {

    private static final Logger logger = LoggerFactory.getLogger(PrivacyExtractor.class);

    private static final String DEFAULT_CONSENT_VALUE = "";
    private static final String DEFAULT_GDPR_VALUE = "";
    private static final Ccpa DEFAULT_CCPA_VALUE = Ccpa.EMPTY;

    /**
     * Retrieves:
     * <p><ul>
     * <li>gdpr from regs.ext.gdpr
     * <li>us_privacy from regs.ext.us_privacy
     * <li>consent from user.ext.consent
     * </ul><p>
     * And construct {@link Privacy} from them. Use default values in case of invalid value.
     */
    public Privacy validPrivacyFrom(Regs regs, User user, List<String> errors) {
        final ExtRegs extRegs = regs != null ? regs.getExt() : null;
        final ExtUser extUser = user != null ? user.getExt() : null;

        final Integer extRegsGdpr = extRegs != null ? extRegs.getGdpr() : null;
        final String gdpr = extRegsGdpr != null ? Integer.toString(extRegsGdpr) : null;
        final String consent = extUser != null ? extUser.getConsent() : null;
        final String usPrivacy = extRegs != null ? extRegs.getUsPrivacy() : null;

        return toValidPrivacy(gdpr, consent, usPrivacy, errors);
    }

    private static Privacy toValidPrivacy(String gdpr, String consent, String usPrivacy, List<String> errors) {
        final String validGdpr = ObjectUtils.notEqual(gdpr, "1") && ObjectUtils.notEqual(gdpr, "0")
                ? DEFAULT_GDPR_VALUE
                : gdpr;
        final String validConsent = consent == null ? DEFAULT_CONSENT_VALUE : consent;
        final Ccpa validCCPA = usPrivacy == null ? DEFAULT_CCPA_VALUE : toValidCcpa(usPrivacy, errors);
        return Privacy.of(validGdpr, validConsent, validCCPA);
    }

    private static Ccpa toValidCcpa(String usPrivacy, List<String> errors) {
        try {
            Ccpa.validateUsPrivacy(usPrivacy);
            return Ccpa.of(usPrivacy);
        } catch (PreBidException e) {
            final String message = String.format("CCPA consent %s has invalid format: %s", usPrivacy, e.getMessage());
            logger.debug(message);
            errors.add(message);
            return DEFAULT_CCPA_VALUE;
        }
    }
}
