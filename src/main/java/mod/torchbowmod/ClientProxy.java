package mod.torchbowmod;


import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy implements CommonProxy{
	/*
	@Override
	public int getNewRenderType() {
		// TODO 自動生成されたメソッド・スタブ
		return RenderingRegistry.getNextAvailableRenderId();
	}
	*/
	@Override
	public void registerRenderes() {
		RenderingRegistry.registerEntityRenderingHandler(EntityTorch.class, RenderTorch::new);
	}
	public void registerRenderThings(){
		//描画の登録
		//MinecraftForgeClient.registerItemRenderer(TorchBowMod.torchbow, new RenderAnimationBow());

	}
}
