package com.kristoff.robomaster_simulator.systems.buffs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.kristoff.robomaster_simulator.robomasters.RoboMaster;
import com.kristoff.robomaster_simulator.robomasters.Enemy;
import com.kristoff.robomaster_simulator.robomasters.Allies;
import com.kristoff.robomaster_simulator.systems.Systems;
import com.kristoff.robomaster_simulator.teams.Team;
import com.kristoff.robomaster_simulator.utils.Position;
import com.kristoff.robomaster_simulator.view.actors.CustomActor;
import com.kristoff.robomaster_simulator.view.ui.controls.Image;
import com.kristoff.robomaster_simulator.view.ui.controls.UIElement;

public class BuffZone {
    Buff buff;
    boolean isActive;
    String name;
    CustomActor actor;
    Image buffImage;
    TextureMapObject textureMapObject;
    Position centrePosition;


    public static int isHPRecoveryNeeded = 0;
    public static int isBulletSupplyNeeded = 0;
    public static int isRedHPRecoveryNecessary = 0;

    static BuffZone NotActivated    ;
    static BuffZone RedHPRecovery   ;
    static BuffZone DisableShooting ;
    static BuffZone BlueBulletSupply;
    static BuffZone BlueHPRecovery  ;
    static BuffZone DisableMovement ;
    static BuffZone RedBulletSupply ;

    public BuffZone(TextureMapObject textureMapObject){
        this.name = textureMapObject.getName();
        this.isActive = false;
        buffImage = new Image();
        updateBuff(Buff.Unknown, false);
        this.textureMapObject = textureMapObject;
        float scale = 1f / 1000f;
        this.actor = new CustomActor(textureMapObject.getTextureRegion());
        actor.setX(textureMapObject.getX() * scale);
        actor.setY(textureMapObject.getY() * scale);
        actor.setWidth(textureMapObject.getTextureRegion().getRegionWidth() * scale);
        actor.setHeight(textureMapObject.getTextureRegion().getRegionHeight() * scale);
        centrePosition = new Position(
                (int)((textureMapObject.getX() + textureMapObject.getTextureRegion().getRegionWidth() / 2f) / 10f),
                (int)((textureMapObject.getY() + textureMapObject.getTextureRegion().getRegionHeight() / 2f) / 10f));

        buffImage.setScale(0.006f);
        buffImage.setRelativePosition(
                (textureMapObject.getX() + 75f) * scale,
                (textureMapObject.getY() + 40f) * scale,
                UIElement.HorizontalAlignment.LEFT_ALIGNMENT, UIElement.VerticalAlignment.BOTTOM_ALIGNMENT);
    }

    public static void setEnemyHPRecoveryNeeded() {
        if(RedHPRecovery == null){
            isRedHPRecoveryNecessary = 0;
        }
        else if(RedHPRecovery.isActive){
            if((Enemy.getLockedEnemy().getHealth() >= 1900 || !Enemy.getLockedEnemy().isAlive) &&
                    (Enemy.getUnlockedEnemy().getHealth() >= 1900 || !Enemy.getUnlockedEnemy().isAlive)){
                isRedHPRecoveryNecessary = setPriority(RedHPRecovery);
            }
        }
        else {
            isRedHPRecoveryNecessary = 0;
        }
    }

    public static int setPriority(BuffZone buffZone){
        float distanceToBlue1 = buffZone.centrePosition.distanceTo(Team.friend1.getPointPosition());
        float distanceToBlue2 = buffZone.centrePosition.distanceTo(Team.friend2.getPointPosition());
        if(distanceToBlue1 < distanceToBlue2){
            return 1;
        }
        else {
            return 2;
        }
    }

    public static boolean isEnemyHPRecoveryNeeded(Allies roboMaster){
        if(isRedHPRecoveryNecessary == 0) return false;
        if(roboMaster == Team.friend1){
            return isRedHPRecoveryNecessary == 1;
        }
        else {
            return isRedHPRecoveryNecessary == 2;
        }
    }

    public String getName(){
        return this.name;
    }

    public Buff getBuff(){
        return this.buff;
    }

    public CustomActor getBuffZoneActor() {
        return this.actor;
    }

    public Image getBuffImage() {
        return this.buffImage;
    }

    public static boolean isInDebuffZone(int x, int y){
        for(BuffZone buffZone : Systems.refree.getBuffZones()){
            if(buffZone.getBuff() != Buff.Unknown &&
                    buffZone.getBuff() != Buff.BlueBulletSupply &&
                buffZone.getBuff() != Buff.BlueHPRecovery){
                if(isInBuffZone(x, y, buffZone)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInBuffZone(int x, int y, boolean isTarget){
        Rectangle bounds = this.getBuffZoneActor().getBounds();
        if(!isTarget){
            bounds.x -= 0.25f;
            bounds.y -= 0.25f;
            bounds.width += 0.5f;
            bounds.height += 0.5f;
        }
        return bounds.contains(x / 100f, y / 100f);
    }

    public static int costOfBuff(int x, int y, Allies roboMaster){
        int cost = 0;

        for(BuffZone buffZone : Systems.refree.getBuffZones()){
            if(!buffZone.isActive) continue;

            float distance = buffZone.centrePosition.distanceTo(x,y);
            float maxDis = 150;

            switch (buffZone.buff){
                case Unknown -> cost = 0;
                case RedHPRecovery    -> {
                    if(isEnemyHPRecoveryNeeded(roboMaster)) {
                        if(distance <= 10){
                            cost += (maxDis - distance) / maxDis * -150;
                        }
                       //if(buffZone.isInBuffZone(x, y, true)) cost = -197;
                    }
                    else{
                        if(buffZone.isInBuffZone(x, y, false)) cost = 999;
                    }
                }
                case RedBulletSupply  -> {
                    if (buffZone.isInBuffZone(x, y, false)) cost = 999;
                }
                case BlueHPRecovery   -> {
                    if(isHPRecoveryNeeded(roboMaster)) {
                        if(distance <= 10){
                            cost += (maxDis - distance) / maxDis * -150;
                        }
                        //if(buffZone.isInBuffZone(x, y, true)) cost = -197;
                    }
                    else{
                        if(buffZone.isInBuffZone(x, y, false)) cost = 999;
                    }
//                    if(buffZone.isInBuffZone(x, y, true)) cost = -150;
//                    else if(distance <= maxDis){
//                        cost += (maxDis - distance) / maxDis * -127;
//                    }
                }
                case BlueBulletSupply -> {
//                    if(buffZone.isInBuffZone(x, y, true)) cost = -150;
//                    else if(distance <= maxDis){
//                        cost += (maxDis - distance) / maxDis * -127;
//                    }
                    if(isBulletSupplyNeeded((roboMaster))){
                        if(distance <= 10){
                            cost += (maxDis - distance) / maxDis * -256;
                        }
                    }
                }
                case DisableShooting  -> {
                    if(buffZone.isInBuffZone(x, y, false)) cost = 999;
                }
                case DisableMovement  -> {
                    if(buffZone.isInBuffZone(x, y, false)) cost = 999;
                }
            }
//            else{
//
////                if(Systems.refree.buffRoundIndex == 0 && roboMaster == Team.blue2 && buffZone.buff == Buff.RedBulletSupply){
////                    float distance2 = new Position(x, y).distanceTo(500, 70);
////                    if(distance2 <= 10){
////                        cost += - 255;
////                    }
////                }
//            }
        }
        return cost;
    }

    public boolean isBulletSupplyNeeded(RoboMaster roboMaster){
        return true;
    }

    public static boolean isHPRecoveryNeeded(Allies roboMaster){
        if(isHPRecoveryNeeded == 0) return false;
        if(roboMaster == Team.friend1){
            return isHPRecoveryNeeded == 1;
        }
        else {
            return isHPRecoveryNeeded == 2;
        }
    }

    public static void setHPRecoveryNeeded(){
        if(BlueHPRecovery == null){
            isHPRecoveryNeeded = 0;
        }
        if (Team.friend1.getHealth() > 1800 && Team.friend2.getHealth() > 1800) {
            isHPRecoveryNeeded = 0;
        }
        else if(Team.friend1.getHealth() > 1000 && Team.friend2.getHealth() > 1000){
            isHPRecoveryNeeded = 0;
        }
        else{
            isHPRecoveryNeeded = setPriority(BlueHPRecovery);
        }
    }

    public static boolean isBulletSupplyNeeded(Allies roboMaster){
        if(isBulletSupplyNeeded == 0) return false;
        if(roboMaster == Team.friend1){
            return isBulletSupplyNeeded == 1;
        }
        else {
            return isBulletSupplyNeeded == 2;
        }
    }

    public static void setBulletSupplyNeeded(){
        if(BlueBulletSupply == null){
            return;
        }
        isBulletSupplyNeeded = setPriority(BlueBulletSupply);
    }

    public static boolean isInBuffZone(int x, int y, BuffZone buffZone){
        return buffZone.getBuffZoneActor().getBounds().contains(x / 100f, y / 100f);
    }

    public static void updateBuffZone(int buffZoneNo, int buffType, boolean isActive){
        for(BuffZone buffZone : Systems.refree.getBuffZones()){
            if(buffZone.getName().equals("F" + (buffZoneNo + 1))){
                switch (buffType){
                    case 0 -> {
                        buffZone.updateBuff(Buff.Unknown, isActive);
                        NotActivated = buffZone;
                    }
                    case 1 -> {
                        if(Team.isOurTeamBlue){
                            buffZone.updateBuff(Buff.RedHPRecovery, isActive);
                            RedHPRecovery = buffZone;
                        }
                        else {
                            buffZone.updateBuff(Buff.BlueHPRecovery, isActive);
                            BlueHPRecovery = buffZone;
                        }
                    }
                    case 2 -> {
                        if(Team.isOurTeamBlue) {
                            buffZone.updateBuff(Buff.RedBulletSupply, isActive);
                            RedBulletSupply = buffZone;
                        }
                        else {
                            buffZone.updateBuff(Buff.BlueBulletSupply, isActive);
                            BlueBulletSupply = buffZone;
                        }
                    }
                    case 3 -> {
                        if(Team.isOurTeamBlue) {
                            buffZone.updateBuff(Buff.BlueHPRecovery, isActive);
                            BlueHPRecovery = buffZone;
                        }
                        else {
                            buffZone.updateBuff(Buff.RedHPRecovery, isActive);
                            RedHPRecovery = buffZone;
                        }
                    }
                    case 4 -> {
                        if(Team.isOurTeamBlue) {
                            buffZone.updateBuff(Buff.BlueBulletSupply, isActive);
                            BlueBulletSupply = buffZone;
                        }
                        else {
                            buffZone.updateBuff(Buff.RedBulletSupply, isActive);
                            RedBulletSupply = buffZone;
                        }
                    }
                    case 5 -> {
                        buffZone.updateBuff(Buff.DisableShooting, isActive);
                        DisableShooting = buffZone;
                    }
                    case 6 -> {
                        buffZone.updateBuff(Buff.DisableMovement, isActive);
                        DisableMovement = buffZone;
                    }
                }
                break;
            }
        }
    }


    public void updateBuff(Buff buff, boolean isActive){
        this.buff = buff;
        String pathHeader = "Systems/BuffZones/";
        if(isActive || this.buff == Buff.Unknown){
            switch (buff){
                case Unknown -> pathHeader += "Unknown.png";
                case RedHPRecovery     -> pathHeader += "HealingRed.png";
                case DisableShooting   -> pathHeader += "ShootingForbidden.png";
                case BlueBulletSupply  -> pathHeader += "BulletSupplyBlue.png";
                case BlueHPRecovery    -> pathHeader += "HealingBlue.png";
                case DisableMovement   -> pathHeader += "MovementForbidden.png";
                case RedBulletSupply   -> pathHeader += "BulletSupplyRed.png";
            }
        }
        else {
            pathHeader += "NotActivated.png";
        }
        this.isActive = isActive;
        String finalPathHeader = pathHeader;
        Gdx.app.postRunnable(new Runnable()
        {
            @Override
            public void run()
            {
                buffImage.setTextureRegion(finalPathHeader);
            }
        });
    }
}
