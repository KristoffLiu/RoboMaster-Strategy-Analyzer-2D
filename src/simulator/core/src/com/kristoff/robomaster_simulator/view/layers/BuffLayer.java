package com.kristoff.robomaster_simulator.view.layers;

import com.kristoff.robomaster_simulator.systems.Systems;
import com.kristoff.robomaster_simulator.systems.buffs.BuffZone;
import com.kristoff.robomaster_simulator.view.renderers.EnvRenderer;
import com.kristoff.robomaster_simulator.view.ui.pages.UIPage;

public class BuffLayer extends UIPage {

    public BuffLayer(EnvRenderer envRenderer) {
        super(envRenderer.view.getViewport());
        addBuffZones();
    }

    public void addBuffZones(){
        for(BuffZone buffZone : Systems.refree.getBuffZones()){
            this.addActor(buffZone.getBuffZoneActor());
            this.addUIElement(buffZone.getBuffImage());
        }
    }

    @Override
    public void act (float delta) {
        super.act(delta);
    }

    @Override
    public void draw(){
        super.draw();
    }
}
