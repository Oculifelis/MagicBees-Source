package com.magicbees;

import com.accbdd.complicated_bees.recipe.mutation.condition.IMutationCondition;
import com.accbdd.complicated_bees.registry.MutationRegistration;
import com.magicbees.condition.MoonPhaseCondition;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Mutation conditions added to Complicated Bees' registry; recipes name them by id in their "conditions" object. */
public final class MBConditions {
    public static final DeferredRegister<IMutationCondition> CONDITIONS = DeferredRegister.create(MutationRegistration.MUTATION_CONDITION_KEY, MagicBees.MODID);

    public static final Supplier<IMutationCondition> MOON_PHASE = CONDITIONS.register(MoonPhaseCondition.ID, () -> new MoonPhaseCondition(0, 0));

    private MBConditions() {
    }

    static void register(IEventBus bus) {
        CONDITIONS.register(bus);
    }
}
