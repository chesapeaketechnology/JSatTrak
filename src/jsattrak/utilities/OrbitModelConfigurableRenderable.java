package jsattrak.utilities;

import gov.nasa.worldwind.globes.Globe;
import jsattrak.objects.AbstractSatellite;

import java.util.Hashtable;
import java.util.Map;

/**
 * Create a more configurable variant of the {@link OrbitModelRenderable}.
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
     * Creates a new instance of OrbitModel
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

    @Override
    protected boolean shouldRenderLeadingOrbit(AbstractSatellite satellite)
    {
        boolean configSetting = orbitModelRenderingConfiguration.getOrDefault(OrbitModelRenderConfig.LEADING_ORBIT, true);
        return configSetting && super.shouldRenderLeadingOrbit(satellite);
    }

    @Override
    protected boolean shouldRenderLaggingOrbit(AbstractSatellite satellite)
    {
        boolean configSetting = orbitModelRenderingConfiguration.getOrDefault(OrbitModelRenderConfig.LAGGING_ORBIT, true);
        return configSetting && super.shouldRenderLaggingOrbit(satellite);
    }

    @Override
    protected boolean shouldRender3dName(AbstractSatellite satellite)
    {
        boolean configSetting = orbitModelRenderingConfiguration.getOrDefault(OrbitModelRenderConfig.SATELLITE_NAME_3D, true);
        return configSetting && super.shouldRender3dName(satellite);
    }

    @Override
    protected boolean shouldRender3dFootprint(AbstractSatellite satellite)
    {
        boolean configSetting = orbitModelRenderingConfiguration.getOrDefault(OrbitModelRenderConfig.COVERAGE_FOOTPRINT_3D, true);
        return configSetting && super.shouldRender3dFootprint(satellite);
    }
}
