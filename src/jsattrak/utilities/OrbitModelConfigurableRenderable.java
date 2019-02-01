package jsattrak.utilities;

import gov.nasa.worldwind.globes.Globe;
import jsattrak.objects.AbstractSatellite;

import java.util.Hashtable;
import java.util.Map;

/**
 * Defines a more configurable variant of the {@link OrbitModelRenderable}.
 *
 * @author Proprietary information subject to the terms of a Non-Disclosure Agreement
 * @since CTI-1.0.0
 */
public class OrbitModelConfigurableRenderable extends OrbitModelRenderable
{
    public enum OrbitModelRenderConfig
    {
        LEADING_ORBIT, LAGGING_ORBIT, SATELLITE_NAME_3D, COVERAGE_FOOTPRINT_3D
    }

    private final Map<OrbitModelRenderConfig, Boolean> orbitModelRenderingConfiguration;

    /**
     * Creates a new instance of OrbitModelConfigurableRenderable which is no different than
     * {@link OrbitModelRenderable} except that it contains a configuration map which
     * allows for multiple instances of it to be created using the same list of
     * satellites as the underlying data set, but then each renderable can render that
     * same set of satellites in a different way. For example, if you want two layers,
     * one with just the orbit path and one with just the 3D coverage "cone", previously
     * you would have had to create two {@link Hashtable}s of the {@link AbstractSatellite}s
     * of interest, then for one you set them to only render the orbit model and for the
     * other you set them to only render the cone.
     *
     * @param includedSatellites hashtable of satellites to include in the rendering
     * @param globe              on which to render the satellite renderables
     */
    public OrbitModelConfigurableRenderable(Hashtable<String, AbstractSatellite> includedSatellites,
                                            Map<OrbitModelRenderConfig, Boolean> orbitModelRenderingConfiguration,
                                            Globe globe)
    {
        super(includedSatellites, globe, RenderableIconType.NONE);
        this.orbitModelRenderingConfiguration = orbitModelRenderingConfiguration;
    }

    /**
     * The {@link Map} {@code getOrDefault} API isn't available until JDK 8 and this
     * requires JDK6 level of support, so recreate the capability with this helper
     * method.
     *
     * @param key          the configuration key requested
     * @param defaultValue the default value if the configuration map doesn't contain that key
     * @return a boolean from the  configuration for the  given key or else a provided
     * default value
     */
    private boolean getConfigBooleanOrDefault(OrbitModelRenderConfig key, boolean defaultValue)
    {
        boolean configSetting = defaultValue;
        if (orbitModelRenderingConfiguration.containsKey(key))
        {
            configSetting = orbitModelRenderingConfiguration.get(key);
        }
        return configSetting;
    }


    @Override
    protected boolean shouldRenderLeadingOrbit(AbstractSatellite satellite)
    {
        boolean configSetting = getConfigBooleanOrDefault(OrbitModelRenderConfig.LEADING_ORBIT, true);
        return configSetting && super.shouldRenderLeadingOrbit(satellite);
    }

    @Override
    protected boolean shouldRenderLaggingOrbit(AbstractSatellite satellite)
    {
        boolean configSetting = getConfigBooleanOrDefault(OrbitModelRenderConfig.LAGGING_ORBIT, true);
        return configSetting && super.shouldRenderLaggingOrbit(satellite);
    }

    @Override
    protected boolean shouldRender3dName(AbstractSatellite satellite)
    {
        boolean configSetting = getConfigBooleanOrDefault(OrbitModelRenderConfig.SATELLITE_NAME_3D, true);
        return configSetting && super.shouldRender3dName(satellite);
    }

    @Override
    protected boolean shouldRender3dFootprint(AbstractSatellite satellite)
    {
        boolean configSetting = getConfigBooleanOrDefault(OrbitModelRenderConfig.COVERAGE_FOOTPRINT_3D, true);
        return configSetting && super.shouldRender3dFootprint(satellite);
    }
}
