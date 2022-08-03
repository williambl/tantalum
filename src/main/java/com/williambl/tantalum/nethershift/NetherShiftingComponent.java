package com.williambl.tantalum.nethershift;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;

import static com.williambl.tantalum.Tantalum.id;

public interface NetherShiftingComponent extends Component, ServerTickingComponent, ClientTickingComponent, AutoSyncedComponent {
    void startShift(int ticksToShift);
    boolean isCharging();
    boolean isShifting();

    ComponentKey<NetherShiftingComponent> KEY = ComponentRegistry.getOrCreate(id("nether_shifting"), NetherShiftingComponent.class);
}
