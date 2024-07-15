package mafuyu33.fallingworld;


import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;


public class FallingWorld implements ModInitializer {
    public static final String MOD_ID = "falling-world";
	public static final GameRules.Key<GameRules.IntRule> FALLING_RANGE_HORIZONTAL =
			GameRuleRegistry.register("fallingRange_HorizontalRadius", GameRules.Category.UPDATES, GameRuleFactory.createIntRule(6,0));
	public static final GameRules.Key<GameRules.IntRule> FALLING_RANGE_VERTICAL =
			GameRuleRegistry.register("fallingRange_VerticalRadius", GameRules.Category.UPDATES, GameRuleFactory.createIntRule(20,0));
	@Override
	public void onInitialize() {
//		UseBlockCallback.EVENT.register(new UseBlockHandler());
	}
}