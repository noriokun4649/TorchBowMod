package mod.torchbowmod;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraftforge.client.MinecraftForgeClient;

public class ClientProxy implements CommonProxy{

	@Override
	public int getNewRenderType() {
		// TODO 自動生成されたメソッド・スタブ
		return RenderingRegistry.getNextAvailableRenderId();
	}
	@Override
	public void registerRenderes() {
		RenderingRegistry.registerEntityRenderingHandler(EntityTorch.class, new RenderTorch());
	}
	public void registerRenderThings(){
		//描画の登録
		MinecraftForgeClient.registerItemRenderer(TorchBowMod.torchbow, new RenderAnimationBow());

	}
}
