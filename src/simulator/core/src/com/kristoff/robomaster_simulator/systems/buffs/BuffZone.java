package com.kristoff.robomaster_simulator.systems.buffs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.kristoff.robomaster_simulator.robomasters.RoboMaster;
import com.kristoff.robomaster_simulator.robomasters.Enemy;
import com.kristoff.robomaster_simulator.robomasters.Ally;
import com.kristoff.robomaster_simulator.robomasters.modules.TeamColor;
import com.kristoff.robomaster_simulator.systems.Systems;
import com.kristoff.robomaster_simulator.teams.allies.Allies;
import com.kristoff.robomaster_simulator.utils.Position;
import com.kristoff.robomaster_simulator.view.actors.CustomActor;
import com.kristoff.robomaster_simulator.view.ui.controls.Image;
import com.kristoff.robomaster_simulator.view.ui.controls.UIElement;
import org.lwjgl.Sys;

public class BuffZone {
    Buff buff;
    boolean isActive;
    String name;
    CustomActor actor;
    Image buffImage;
    TextureMapObject textureMapObject;
    Position centrePosition;
    Ally neededAlly;

    public static int isHPRecoveryNeeded = 0;
    public static int isBulletSupplyNeeded = 0;
    public static int isRedHPRecoveryNecessary = 0;

    public static BuffZone NotActivated    ;
    public static BuffZone RedHPRecovery   ;
    public static BuffZone DisableShooting ;
    public static BuffZone BlueBulletSupply;
    public static BuffZone BlueHPRecovery  ;
    public static BuffZone DisableMovement ;
    public static BuffZone RedBulletSupply ;

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
        if(Allies.teamColor == TeamColor.BLUE){
            if(RedHPRecovery == null){
                isRedHPRecoveryNecessary = 0;
            }
            else if(RedHPRecovery.isActive){
                if((Enemy.getLockedEnemy().getHealth() > 1900 || !Enemy.getLockedEnemy().isAlive) &&
                        (Enemy.getUnlockedEnemy().getHealth() > 1900 || !Enemy.getUnlockedEnemy().isAlive)){
                    isRedHPRecoveryNecessary = setPriority(RedHPRecovery);
                }
                else{

                }
            }
            else {
                isRedHPRecoveryNecessary = 0;
            }
        }
        else{
            if(BlueHPRecovery == null){
                isRedHPRecoveryNecessary = 0;
            }
            else if(BlueHPRecovery.isActive){
                if((Enemy.getLockedEnemy().getHealth() > 1900 || !Enemy.getLockedEnemy().isAlive) &&
                        (Enemy.getUnlockedEnemy().getHealth() > 1900 || !Enemy.getUnlockedEnemy().isAlive)){
                    isRedHPRecoveryNecessary = setPriority(BlueHPRecovery);
                }
                else{

                }
            }
            else {
                isRedHPRecoveryNecessary = 0;
            }
        }
    }

    public static int setPriority(BuffZone buffZone){
        if(Systems.refree.remainingTime == -1 || Systems.refree.remainingTime > 175){
            if((buffZone.getName().equals("F1") && Allies.teamColor == TeamColor.BLUE) || (buffZone.getName().equals("F6") && Allies.teamColor == TeamColor.RED)){
                return 2;
            }
            else{
                return 1;
            }
        }
        else {
            float distanceToBlue1 = buffZone.centrePosition.distanceTo(Allies.ally1.getPointPosition());
            float distanceToBlue2 = buffZone.centrePosition.distanceTo(Allies.ally2.getPointPosition());
            if((Allies.ally1.isAlive() && !Allies.ally2.isAlive()) || (distanceToBlue1 < distanceToBlue2)){
                return 1;
            }
            else if((Allies.ally2.isAlive() && !Allies.ally1.isAlive()) || (distanceToBlue1 > distanceToBlue2)){
                return 2;
            }
            else{
                return 1;
            }
        }
    }

    public static boolean isEnemyHPRecoveryNeeded(Ally roboMaster){
        if(isRedHPRecoveryNecessary == 0) return false;
        if(roboMaster == Allies.ally1){
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

    public Position getPosition(int x, int y, boolean isTarget){
        Rectangle bounds = this.getBuffZoneActor().getBounds();
        return new Position((int)(bounds.getX() * 100), (int)(bounds.getY()*100));
    }

    public static int costOfBuff(int x, int y, Ally roboMaster){
        int cost = 0;

        for(BuffZone buffZone : Systems.refree.getBuffZones()){
            if(!buffZone.isActive) continue;

            float distance = buffZone.centrePosition.distanceTo(x,y);
            float maxDis = 150;

            if(Allies.teamColor == TeamColor.BLUE){
                switch (buffZone.buff){
                    case Unknown -> cost = 0;
                    case RedHPRecovery    -> {
                        if(isEnemyHPRecoveryNeeded(roboMaster)) {
                            if(distance <= 10){
                                cost += (maxDis - distance) / maxDis * -180;
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
                                cost += (maxDis - distance) / maxDis * -210;
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
                                cost += (maxDis - distance) / maxDis * -200;
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
            }
            else {
                switch (buffZone.buff) {
                    case Unknown -> cost = 0;
                    case RedHPRecovery -> {
                        if (isHPRecoveryNeeded(roboMaster)) {
                            if (distance <= 10) {
                                cost += (maxDis - distance) / maxDis * -210;
                            }
                            //if(buffZone.isInBuffZone(x, y, true)) cost = -197;
                        } else {
                            if (buffZone.isInBuffZone(x, y, false)) cost = 999;
                        }
                    }
                    case RedBulletSupply -> {
                        if (isBulletSupplyNeeded((roboMaster))) {
                            if (distance <= 10) {
                                cost += (maxDis - distance) / maxDis * -200;
                            }
                        }
                    }
                    case BlueHPRecovery -> {
                        if (isEnemyHPRecoveryNeeded(roboMaster)) {
                            if (distance <= 10) {
                                cost += (maxDis - distance) / maxDis * -180;
                            }
                            //if(buffZone.isInBuffZone(x, y, true)) cost = -197;
                        } else {
                            if (buffZone.isInBuffZone(x, y, false)) cost = 999;
                        }
                    }
                    case BlueBulletSupply -> {
                        if (buffZone.isInBuffZone(x, y, false)) cost = 999;
                    }
                    case DisableShooting -> {
                        if (buffZone.isInBuffZone(x, y, false)) cost = 999;
                    }
                    case DisableMovement -> {
                        if (buffZone.isInBuffZone(x, y, false)) cost = 999;
                    }
                }
            }
        }
        return cost;
    }

    public boolean isBulletSupplyNeeded(RoboMaster roboMaster){
        return true;
    }

    public static boolean isHPRecoveryNeeded(Ally roboMaster){
        if(isHPRecoveryNeeded == 0) return false;
        if(roboMaster == Allies.ally1){
            return isHPRecoveryNeeded == 1;
        }
        else {
            return isHPRecoveryNeeded == 2;
        }
    }

    public static void setHPRecoveryNeeded(){
        if(Allies.teamColor == TeamColor.BLUE) {
            if (BlueHPRecovery == null) {
                isHPRecoveryNeeded = 0;
            }
            else if(!BlueHPRecovery.isActive()) isHPRecoveryNeeded = 0;
            else if (Allies.ally1.getHealth() > 1800 && Allies.ally2.getHealth() > 1800) {
                isHPRecoveryNeeded = 0;
            } else if (Allies.ally1.getHealth() > 1000 && Allies.ally2.getHealth() > 1000) {
                isHPRecoveryNeeded = 0;
            } else {
                isHPRecoveryNeeded = setPriority(BlueHPRecovery);
            }
        }
        else {
            if (RedHPRecovery == null) {
                isHPRecoveryNeeded = 0;
            }
            else if(!RedHPRecovery.isActive()) isHPRecoveryNeeded = 0;
            else if (Allies.ally1.getHealth() > 1800 && Allies.ally2.getHealth() > 1800) {
                isHPRecoveryNeeded = 0;
            } else if (Allies.ally1.getHealth() > 1000 && Allies.ally2.getHealth() > 1000) {
                isHPRecoveryNeeded = 0;
            } else {
                isHPRecoveryNeeded = setPriority(RedHPRecovery);
            }
        }
    }

    public static boolean isBulletSupplyNeeded(Ally roboMaster){
        if(isBulletSupplyNeeded == 0) return false;
        if(roboMaster == Allies.ally1){
            return isBulletSupplyNeeded == 1;
        }
        else {
            return isBulletSupplyNeeded == 2;
        }
    }

    public static void setBulletSupplyNeeded(){
        if(Allies.teamColor == TeamColor.BLUE) {
            if (BlueBulletSupply == null) {
                return;
            }
            else if(!BlueBulletSupply.isActive) isBulletSupplyNeeded = 0;
            else isBulletSupplyNeeded = setPriority(BlueBulletSupply);
        }
        else {
            if (RedBulletSupply == null) {
                return;
            }
            else if(!RedBulletSupply.isActive) isBulletSupplyNeeded = 0;
            else isBulletSupplyNeeded = setPriority(RedBulletSupply);
        }
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
                        buffZone.updateBuff(Buff.RedHPRecovery, isActive);
                        RedHPRecovery = buffZone;
                    }
                    case 2 -> {
                        buffZone.updateBuff(Buff.RedBulletSupply, isActive);
                        RedBulletSupply = buffZone;
                    }
                    case 3 -> {
                        buffZone.updateBuff(Buff.BlueHPRecovery, isActive);
                        BlueHPRecovery = buffZone;
                    }
                    case 4 -> {
                        buffZone.updateBuff(Buff.BlueBulletSupply, isActive);
                        BlueBulletSupply = buffZone;
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

    public boolean isActive(){
        return this.isActive;
    }

    public static BuffZone AllyHPRecoveryBuffZone(){
        if(Allies.teamColor == TeamColor.BLUE){
            return BlueHPRecovery;
        }
        else if(Allies.teamColor == TeamColor.RED){
            return RedHPRecovery;
        }
        return BlueHPRecovery;
    }

    public static BuffZone AllyBulletSupplyBuffZone(){
        if(Allies.teamColor == TeamColor.BLUE){
            return BlueBulletSupply;
        }
        else if(Allies.teamColor == TeamColor.RED){
            return RedBulletSupply;
        }
        return BlueBulletSupply;
    }

    public static BuffZone EnemyHPRecoveryBuffZone(){
        if(Allies.teamColor == TeamColor.BLUE){
            return RedHPRecovery;
        }
        else if(Allies.teamColor == TeamColor.RED){
            return BlueHPRecovery;
        }
        return RedHPRecovery;
    }

    public static boolean isAnyAvailableBuffZone(){
        return AllyHPRecoveryBuffZone().isActive() || AllyBulletSupplyBuffZone().isActive() || EnemyHPRecoveryBuffZone().isActive();
    }
}
