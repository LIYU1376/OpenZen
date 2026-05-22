package shit.zen.modules.impl.world;

import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.network.webui.CategoriesHandler;
import shit.zen.network.webui.ModulesHandler;
import shit.zen.network.webui.SetSettingHandler;
import shit.zen.network.webui.SettingsHandler;
import shit.zen.network.webui.StaticFileHandler;
import shit.zen.network.webui.ToggleModuleHandler;
import shit.zen.settings.impl.BooleanSetting;
import shit.zen.settings.impl.NumberSetting;
import shit.zen.utils.misc.ChatUtil;

public class WebUI extends Module {
    private final NumberSetting port = new NumberSetting("Port", 8089, 1024, 65535, 1);
    private final BooleanSetting openBrowser = new BooleanSetting("Open Browser", true);

    private HttpServer httpServer;

    public WebUI() {
        super("WebUI", Category.WORLD);
    }

    @Override
    public void onEnable() {
        try {
            this.httpServer = this.createHttpServer();
            ChatUtil.print("WebUI started at http://127.0.0.1:" + this.port.getValue().intValue());
            if (this.openBrowser.getValue() && mc.player != null) {
                try {
                    System.setProperty("java.awt.headless", "false");
                    if (Desktop.isDesktopSupported()
                            && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI("http://127.0.0.1:" + this.port.getValue().intValue()));
                    }
                } catch (URISyntaxException | IOException ex) {
                    ChatUtil.print("Failed to open browser: " + ex.getMessage());
                }
            }
        } catch (IOException ioException) {
            ChatUtil.print("Failed to start http server because " + ioException.getMessage());
            ioException.printStackTrace();
            this.setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        if (this.httpServer != null) {
            this.httpServer.stop(0);
            this.httpServer = null;
            ChatUtil.print("WebUI stopped");
        }
    }

    private HttpServer createHttpServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(this.port.getValue().intValue()), 0);
        server.createContext("/api/modulesList", new ModulesHandler());
        server.createContext("/api/categoriesList", new CategoriesHandler());
        server.createContext("/api/setStatus", new ToggleModuleHandler());
        server.createContext("/api/setModuleSettingValue", new SetSettingHandler());
        server.createContext("/api/getModuleSetting", new SettingsHandler());
        server.createContext("/", new StaticFileHandler("/webui", "/"));
        server.start();
        return server;
    }
}
