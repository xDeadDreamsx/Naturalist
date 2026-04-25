package com.starfish_studios.naturalist.registry;

import com.starfish_studios.naturalist.Naturalist;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class NaturalistSoundEvents {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Naturalist.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_AMBIENT = register("entity.tortoise.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_HIDE = register("entity.tortoise.hide");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_THUD = register("entity.tortoise.thud");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_HURT = register("entity.tortoise.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_DEATH = register("entity.tortoise.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_AMBIENT_BABY = register("entity.tortoise.ambient_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_HURT_BABY = register("entity.tortoise.hurt_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_DEATH_BABY = register("entity.tortoise.death_baby");

    public static final DeferredHolder<SoundEvent, SoundEvent> SNAKE_HISS = register("entity.snake.hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNAKE_HURT = register("entity.snake.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNAKE_DEATH = register("entity.snake.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNAKE_AMBIENT = register("entity.snake.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNAKE_RATTLE = register("entity.snake.rattle");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNAIL_AMBIENT = register("entity.snail.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNAIL_HURT = register("entity.snail.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNAIL_CRUSH = register("entity.snail.crush");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNAIL_FORWARD = register("entity.snail.forward");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNAIL_BACK = register("entity.snail.back");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUCKET_FILL_SNAIL = register("item.bucket.fill_snail");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUCKET_EMPTY_SNAIL = register("item.bucket.empty_snail");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRD_HURT = register("entity.bird.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRD_DEATH = register("entity.bird.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRD_EAT = register("entity.bird.eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRD_FLY = register("entity.bird.fly");

    public static final DeferredHolder<SoundEvent, SoundEvent> BIRD_AMBIENT_BLUEJAY = register("entity.bird.ambient_bluejay");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRD_AMBIENT_CANARY = register("entity.bird.ambient_canary");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRD_AMBIENT_ROBIN = register("entity.bird.ambient_robin");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRD_AMBIENT_CARDINAL = register("entity.bird.ambient_cardinal");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRD_AMBIENT_FINCH = register("entity.bird.ambient_finch");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRD_AMBIENT_SPARROW = register("entity.bird.ambient_sparrow");

    public static final DeferredHolder<SoundEvent, SoundEvent> FIREFLY_AMBIENT = register("entity.firefly.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> FIREFLY_HURT = register("entity.firefly.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> FIREFLY_DEATH = register("entity.firefly.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> FIREFLY_HIDE = register("entity.firefly.hide");

    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_HURT = register("entity.bear.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_DEATH = register("entity.bear.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_AMBIENT = register("entity.bear.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_AMBIENT_BABY = register("entity.bear.ambient_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_HURT_BABY = register("entity.bear.hurt_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_DEATH_BABY = register("entity.bear.death_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_SLEEP = register("entity.bear.sleep");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_SNIFF = register("entity.bear.sniff");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_SPIT = register("entity.bear.spit");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_EAT = register("entity.bear.eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_AMBIENT = register("entity.deer.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_HURT = register("entity.deer.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_DEATH = register("entity.deer.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_AMBIENT_BABY = register("entity.deer.ambient_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_HURT_BABY = register("entity.deer.hurt_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_DEATH_BABY = register("entity.deer.death_baby");

    public static final DeferredHolder<SoundEvent, SoundEvent> RHINO_SCRAPE = register("entity.rhino.scrape");
    public static final DeferredHolder<SoundEvent, SoundEvent> RHINO_AMBIENT = register("entity.rhino.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> RHINO_AMBIENT_BABY = register("entity.rhino.ambient_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> RHINO_HURT = register("entity.rhino.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> RHINO_DEATH = register("entity.rhino.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> LION_HURT = register("entity.lion.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> LION_DEATH = register("entity.lion.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> LION_AMBIENT = register("entity.lion.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> LION_ROAR = register("entity.lion.roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> LION_AMBIENT_BABY = register("entity.lion.ambient_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> LION_HURT_BABY = register("entity.lion.hurt_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> LION_DEATH_BABY = register("entity.lion.death_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_HURT = register("entity.elephant.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_DEATH = register("entity.elephant.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_AMBIENT = register("entity.elephant.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_AMBIENT_BABY = register("entity.elephant.ambient_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_HURT_BABY = register("entity.elephant.hurt_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_DEATH_BABY = register("entity.elephant.death_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZEBRA_AMBIENT = register("entity.zebra.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZEBRA_HURT = register("entity.zebra.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZEBRA_DEATH = register("entity.zebra.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZEBRA_EAT = register("entity.zebra.eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZEBRA_BREATHE = register("entity.zebra.breathe");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZEBRA_ANGRY = register("entity.zebra.angry");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZEBRA_JUMP = register("entity.zebra.jump");
    public static final DeferredHolder<SoundEvent, SoundEvent> VULTURE_AMBIENT = register("entity.vulture.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> VULTURE_HURT = register("entity.vulture.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> VULTURE_DEATH = register("entity.vulture.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRAFFE_AMBIENT = register("entity.giraffe.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRAFFE_HURT = register("entity.giraffe.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRAFFE_DEATH = register("entity.giraffe.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRAFFE_AMBIENT_BABY = register("entity.giraffe.ambient_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRAFFE_HURT_BABY = register("entity.giraffe.hurt_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRAFFE_DEATH_BABY = register("entity.giraffe.death_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> HIPPO_AMBIENT = register("entity.hippo.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> HIPPO_HURT = register("entity.hippo.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> HIPPO_DEATH = register("entity.hippo.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOAR_AMBIENT = register("entity.boar.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOAR_HURT = register("entity.boar.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOAR_DEATH = register("entity.boar.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOAR_AMBIENT_BABY = register("entity.boar.ambient_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOAR_HURT_BABY = register("entity.boar.hurt_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOAR_DEATH_BABY = register("entity.boar.death_baby");

    public static final DeferredHolder<SoundEvent, SoundEvent> GATOR_EGG_BREAK = register("entity.alligator.egg_break");
    public static final DeferredHolder<SoundEvent, SoundEvent> GATOR_EGG_CRACK = register("entity.alligator.egg_crack");
    public static final DeferredHolder<SoundEvent, SoundEvent> GATOR_EGG_HATCH = register("entity.alligator.egg_hatch");

    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_EGG_BREAK = register("entity.tortoise.egg_break");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_EGG_CRACK = register("entity.tortoise.egg_crack");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_EGG_HATCH = register("entity.tortoise.egg_hatch");

    public static final DeferredHolder<SoundEvent, SoundEvent> GATOR_AMBIENT = register("entity.alligator.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> GATOR_AMBIENT_BABY = register("entity.alligator.ambient_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> GATOR_HURT = register("entity.alligator.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> GATOR_HURT_BABY = register("entity.alligator.hurt_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> GATOR_DEATH = register("entity.alligator.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> GATOR_DEATH_BABY = register("entity.alligator.death_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> CATFISH_FLOP = register("entity.catfish.flop");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASS_FLOP = register("entity.bass.flop");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGONFLY_LOOP = register("entity.dragonfly.loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGONFLY_HURT = register("entity.dragonfly.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGONFLY_DEATH = register("entity.dragonfly.death");

    public static final DeferredHolder<SoundEvent, SoundEvent> LIZARD_AMBIENT = register("entity.lizard.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIZARD_HURT = register("entity.lizard.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIZARD_DEATH = register("entity.lizard.death");

    public static final DeferredHolder<SoundEvent, SoundEvent> BUTTERFLY_AMBIENT = register("entity.butterfly.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUTTERFLY_HURT = register("entity.butterfly.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> DUCK_AMBIENT = register("entity.duck.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> DUCK_HURT = register("entity.duck.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> DUCK_DEATH = register("entity.duck.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> DUCK_STEP = register("entity.duck.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> DUCK_AMBIENT_BABY = register("entity.duck.ambient_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> DUCK_HURT_BABY = register("entity.duck.hurt_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> DUCK_DEATH_BABY = register("entity.duck.death_baby");

    public static final DeferredHolder<SoundEvent, SoundEvent> RUBBER_DUCKY_AMBIENT = register("entity.rubber_ducky.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> RUBBER_DUCKY_HURT = register("entity.rubber_ducky.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> RUBBER_DUCKY_DEATH = register("entity.rubber_ducky.death");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
