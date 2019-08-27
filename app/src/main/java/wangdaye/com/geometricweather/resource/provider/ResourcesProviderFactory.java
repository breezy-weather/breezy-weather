package wangdaye.com.geometricweather.resource.provider;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

public class ResourcesProviderFactory {

    public static ResourceProvider getNewInstance() {
        return getNewInstance(
                SettingsOptionManager.getInstance(
                        GeometricWeather.getInstance()
                ).getIconProvider()
        );
    }

    public static ResourceProvider getNewInstance(String packageName) {
        Context context = GeometricWeather.getInstance();

        DefaultResourceProvider defaultProvider = new DefaultResourceProvider();

        if (DefaultResourceProvider.isDefaultIconProvider(packageName)) {
            return defaultProvider;
        }

        if (PixelResourcesProvider.isPixelIconProvider(packageName)) {
            return new PixelResourcesProvider(defaultProvider);
        }

        if (IconPackResourcesProvider.isIconPackIconProvider(context, packageName)) {
            return new IconPackResourcesProvider(context, packageName, defaultProvider);
        }

        if (ChronusResourceProvider.isChronusIconProvider(context, packageName)) {
            return new ChronusResourceProvider(context, packageName, defaultProvider);
        }

        return new IconPackResourcesProvider(context, packageName, defaultProvider);
    }

    public static List<ResourceProvider> getProviderList(Context context) {
        List<ResourceProvider> providerList = new ArrayList<>();

        DefaultResourceProvider defaultProvider = new DefaultResourceProvider();

        providerList.add(defaultProvider);
        providerList.add(new PixelResourcesProvider(defaultProvider));

        // geometric weather icon provider.
        providerList.addAll(IconPackResourcesProvider.getProviderList(context, defaultProvider));

        // chronus icon pack.
        List<ChronusResourceProvider> chronusIconPackList
                = ChronusResourceProvider.getProviderList(context, defaultProvider);
        for (int i = chronusIconPackList.size() - 1; i >= 0; i --) {
            for (int j = 0; j < providerList.size(); j ++) {
                if (chronusIconPackList.get(i).equals(providerList.get(j))) {
                    chronusIconPackList.remove(i);
                    break;
                }
            }
        }
        providerList.addAll(chronusIconPackList);

        return providerList;
    }
}
