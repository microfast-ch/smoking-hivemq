package ch.microfast.hivemq.smoker.authz.persistance;

import ch.microfast.hivemq.smoker.authz.common.AuthorizationConsts;
import ch.microfast.hivemq.smoker.authz.domain.Claim;
import ch.microfast.hivemq.smoker.authz.validation.IClaimValidator;
import ch.microfast.hivemq.smoker.authz.validation.InvalidClaimException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.openhft.chronicle.map.ChronicleMap;

public class ChronicleMapDiskClaimStore implements IClaimStore {

    private final IClaimValidator claimValidator;

    private final ChronicleMap<String, Claim> topicClaimMap;

    @Inject
    public ChronicleMapDiskClaimStore(
            IClaimValidator claimValidator,
            @Named("store.expected.max.entries") long expectedMaxEntries,
            @Named("store.file.path") String storeFilePath
    ) throws IOException, NoSuchAlgorithmException {

        this.claimValidator = claimValidator;
        this.topicClaimMap = ChronicleMap
                .of(String.class, Claim.class)
                .name("smoker-topic-claims-map")
                .averageKey(getRandomAverageTopicName())
                .averageValue(new Claim())
                .entries(expectedMaxEntries)
                .createOrRecoverPersistedTo(new File(storeFilePath));
    }

    @Override
    public void upsert(Claim claim) throws InvalidClaimException {
        claimValidator.validateClaim(claim);
        topicClaimMap.put(claim.getRestriction().getTopicName(), claim);
    }

    @Override
    public void delete(String owner, String topicName) {
        topicClaimMap.remove(topicName);
    }

    @Override
    public Collection<Claim> find(Predicate<Claim> filterPredicate) {
        return topicClaimMap.values().stream().filter(filterPredicate).collect(Collectors.toList());
    }

    @Override
    public Collection<Claim> getAll() {
        return  topicClaimMap.values().stream().collect(Collectors.toList());
    }

    /**
     * Generates a random topic with  guessed average topic size
     */
    public String getRandomAverageTopicName() throws NoSuchAlgorithmException {
        byte[] b = new byte[32];
        new Random().nextBytes(b);
        return AuthorizationConsts.RESTRICTED_AREA_PREFIX + "/" + new String(b, StandardCharsets.UTF_8) + "lorem/ipsum/dolor";
    }
}
