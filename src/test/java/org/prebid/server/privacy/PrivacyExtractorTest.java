package org.prebid.server.privacy;

import com.iab.openrtb.request.Regs;
import com.iab.openrtb.request.User;
import org.junit.Before;
import org.junit.Test;
import org.prebid.server.VertxTest;
import org.prebid.server.privacy.ccpa.Ccpa;
import org.prebid.server.privacy.model.Privacy;
import org.prebid.server.proto.openrtb.ext.request.ExtRegs;
import org.prebid.server.proto.openrtb.ext.request.ExtUser;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class PrivacyExtractorTest extends VertxTest {

    private PrivacyExtractor privacyExtractor;

    @Before
    public void setUp() {
        privacyExtractor = new PrivacyExtractor();
    }

    @Test
    public void shouldReturnGdprEmptyValueWhenRegsIsNull() {
        // given and when
        final String gdpr = privacyExtractor.validPrivacyFrom(null, null, new ArrayList<>()).getGdpr();

        // then
        assertThat(gdpr).isEmpty();
    }

    @Test
    public void shouldReturnGdprEmptyValueWhenRegsExtIsNull() {
        // given and when
        final String gdpr = privacyExtractor.validPrivacyFrom(Regs.of(null, null), null, new ArrayList<>()).getGdpr();

        // then
        assertThat(gdpr).isEmpty();
    }

    @Test
    public void shouldReturnGdprEmptyValueWhenRegsExtGdprIsNoEqualsToOneOrZero() {
        // given
        final Regs regs = Regs.of(null, ExtRegs.of(2, null));

        // when
        final String gdpr = privacyExtractor.validPrivacyFrom(regs, null, new ArrayList<>()).getGdpr();

        // then
        assertThat(gdpr).isEmpty();
    }

    @Test
    public void shouldReturnGdprOneWhenExtRegsContainsGdprOne() {
        // given
        final Regs regs = Regs.of(null, ExtRegs.of(1, null));

        // when
        final String gdpr = privacyExtractor.validPrivacyFrom(regs, null, new ArrayList<>()).getGdpr();

        // then
        assertThat(gdpr).isEqualTo("1");
    }

    @Test
    public void shouldReturnGdprZeroWhenExtRegsContainsGdprZero() {
        // given
        final Regs regs = Regs.of(null, ExtRegs.of(0, null));

        // when
        final String gdpr = privacyExtractor.validPrivacyFrom(regs, null, new ArrayList<>()).getGdpr();

        // then
        assertThat(gdpr).isEqualTo("0");
    }

    @Test
    public void shouldReturnConsentEmptyValueWhenExtUserIsNull() {
        // given and when
        final String consent = privacyExtractor.validPrivacyFrom(null, null, new ArrayList<>()).getConsent();

        // then
        assertThat(consent).isEmpty();
    }

    @Test
    public void shouldReturnConsentEmptyValueWhenUserConsentIsNull() {
        // given
        final User user = User.builder().ext(ExtUser.builder().build()).build();

        // when
        final String consent = privacyExtractor.validPrivacyFrom(null, user, new ArrayList<>()).getConsent();

        // then
        assertThat(consent).isEmpty();
    }

    @Test
    public void shouldReturnConsentWhenUserContainsConsent() {
        // given
        final User user = User.builder().ext(ExtUser.builder().consent("consent").build()).build();

        // when
        final String consent = privacyExtractor.validPrivacyFrom(null, user, new ArrayList<>()).getConsent();

        // then
        assertThat(consent).isEqualTo("consent");
    }

    @Test
    public void shouldReturnDefaultCcpaWhenNotValidAndAddError() {
        // given
        final Regs regs = Regs.of(null, ExtRegs.of(null, "invalid"));
        final ArrayList<String> errors = new ArrayList<>();

        // when
        final Ccpa ccpa = privacyExtractor.validPrivacyFrom(regs, null, errors).getCcpa();

        // then
        assertThat(ccpa).isEqualTo(Ccpa.EMPTY);
        assertThat(errors).containsOnly(
                "CCPA consent invalid has invalid format: us_privacy must contain 4 characters");
    }

    @Test
    public void shouldReturnPrivacyWithExtractedParameters() {
        // given
        final Regs regs = Regs.of(null, ExtRegs.of(0, "1Yn-"));
        final User user = User.builder().ext(ExtUser.builder().consent("consent").build()).build();

        // when
        final Privacy privacy = privacyExtractor.validPrivacyFrom(regs, user, new ArrayList<>());

        // then
        assertThat(privacy).isEqualTo(Privacy.of("0", "consent", Ccpa.of("1Yn-")));
    }
}
