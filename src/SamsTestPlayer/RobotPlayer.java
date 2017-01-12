package SamsTestPlayer;
import battlecode.common.*;
import scala.reflect.internal.Trees;

public strictfp class RobotPlayer {
    static RobotController rc;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")

    static int GARDENER_CHANNEL = 5;
    static int GARDENER_MAX = 100;

    //public enum rcstate{none, chop, shake}

    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                runArchon();
                break;
            case GARDENER:
                runGardener();
                break;
            case SOLDIER:
                runSoldier();
                break;
            case LUMBERJACK:
                runLumberjack();
                break;
            case SCOUT:
                runScout();
                break;
        }
	}

    static void runArchon() throws GameActionException {
        System.out.println("I'm an archon!");

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Generate a random direction
                Direction dir = Direction.getWest();
                //int prevNumeGard = rc.readBroadcast(GARDENER_CHANNEL);

                // Randomly attempt to build a gardener in this direction
                //if (prevNumeGard <= GARDENER_MAX &&rc.canBuildRobot(RobotType.GARDENER, dir)) {
                if(rc.getBuildCooldownTurns() == 0){
                    if (rc.canBuildRobot(RobotType.GARDENER, dir)) {
                        rc.hireGardener(dir);
                        //rc.broadcast(GARDENER_CHANNEL, prevNumeGard + 1);
                        rc.broadcast(rc.getID(), 0);
                    }else{
                        tryMove(Direction.getSouth());
                    }
                }


                // Move randomly
                int NumMoves = rc.readBroadcast(rc.getID());
                System.out.println(NumMoves);

                if (NumMoves < 5) {

                    tryMove(Direction.getSouth());

                }
                rc.broadcast(rc.getID(), NumMoves + 1);


                // Broadcast archon's location for other robots on the team to know
                MapLocation myLocation = rc.getLocation();
                rc.broadcast(0,(int)myLocation.x);
                rc.broadcast(1,(int)myLocation.y);

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

	static void runGardener() throws GameActionException {
        System.out.println("I'm a gardener!");

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Listen for home archon's location
                int xPos = rc.readBroadcast(0);
                int yPos = rc.readBroadcast(1);
                MapLocation archonLoc = new MapLocation(xPos,yPos);

                // Generate a random direction
                //Direction dir = randomDirection();

                TreeInfo[] trees = rc.senseNearbyTrees();
                if(trees.length < 5) {
                    if (rc.getTeamBullets() > 50 && rc.getBuildCooldownTurns() == 0) {
                        for (float i = 0; i < 6.2; i = i + (float)0.2) {
                            Direction TempDir = new Direction(i);
                            if (rc.canPlantTree(TempDir)) {
                                rc.plantTree(TempDir);
                                break;
                            }
                        }
                    }


                    //Direction E = new Direction(0);
                    //Direction SE = new Direction((float) 0.785398);
                    //Direction S = new Direction((float) 1.5708);
                    //Direction SW = new Direction((float) 2.35619);
                    //Direction W = new Direction((float) 3.14159);
                    //Direction NW = new Direction((float) 3.92699);
                    //Direction N = new Direction((float) 4.71239);
                    //Direction NE = new Direction((float) 5.49779);

                    //Direction NE = Direction( E.getDeltaX(1), N.getDeltaY(1) );

                    //if you can plant a tree plant one
                    //if (rc.getTeamBullets() > 50 && rc.getBuildCooldownTurns() == 0) {
                    //    if (rc.canPlantTree(E)) {
                    //        rc.plantTree(E);
                    //    } else if (rc.canPlantTree(SE)) {
                    //        rc.plantTree(SE);
                    //    } else if (rc.canPlantTree(S)) {
                    //        rc.plantTree(S);
                    //    } else if (rc.canPlantTree(SW)) {
                    //        rc.plantTree(SW);
                    //    } else if (rc.canPlantTree(W)) {
                    //        rc.plantTree(W);
                    //    } else if (rc.canPlantTree(NW)) {
                    //        rc.plantTree(NW);
                    //    } else if (rc.canPlantTree(N)) {
                    //        rc.plantTree(N);
                    //    } else if (rc.canPlantTree(NE)) {
                     //       rc.plantTree(NE);
                     //   }
                    //}
                }
                //water the lowest HP tree in range

                float LowestTreeHP = 50;
                TreeInfo TargetWaterTree = null;

                for (TreeInfo t : trees) {
                    if (t.health < LowestTreeHP) {
                        LowestTreeHP = t.health;
                        TargetWaterTree = t;
                    }
                }

                if(TargetWaterTree != null){
                    if (rc.canWater(TargetWaterTree.getID() )) {
                        rc.water(TargetWaterTree.getID());
                    }
                }

                //look for trees to shake
                TreeInfo TargetShakeTree = null;
                int NumOfBull = 0;
                for (TreeInfo t : trees) {
                    if (t.containedBullets > NumOfBull) {
                        NumOfBull = t.containedBullets;
                        TargetShakeTree = t;
                    }
                }

                if(TargetShakeTree != null){
                    if (rc.canShake(TargetShakeTree.getID() )) {
                        rc.shake(TargetShakeTree.getID());
                    }
                }


                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }

    static void runSoldier() throws GameActionException {
        System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireSingleShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
                    }
                }

                // Move randomly
                tryMove(randomDirection());

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }

    static void runLumberjack() throws GameActionException {
        System.out.println("I'm a lumberjack!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {



                // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
                RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);

                //check for nearby trees and attempt to chop them down. ~Movian
                TreeInfo[] Trees = rc.senseNearbyTrees();

                MapLocation myLocation = rc.getLocation();

                int action = 0;
                int noofbull = 0;

                TreeInfo TargetTree = null;
                if(Trees.length > 0) {
                    //check for robots or bullets in trees
                    for (int i = 0; i < Trees.length; i++) {
                        //Check if Tree Contains a Robot
                        if (Trees[i].containedRobot != null) {
                            //Tree contains a robot check if tree in range to chop.
                            action = 1;
                            TargetTree = Trees[i];
                            break;

                        } else if (Trees[i].containedBullets > noofbull) {
                            action = 2;
                            noofbull = Trees[i].containedBullets;
                            TargetTree = Trees[i];
                        }
                    }
                }

                System.out.println(action);

                //do something based on action
                switch (action) {
                    case 1:

                        if (rc.canChop(TargetTree.getID())) {
                            rc.chop(TargetTree.getID());
                            System.out.println("I am at loaction:" + myLocation + " Tried to Chop tree with ID:" + TargetTree.getID() + "at location:" + TargetTree.getLocation());
                        } else {
                            //Tree is not in range, move towards tree
                            Direction toTree = myLocation.directionTo(TargetTree.getLocation());
                            tryMove(toTree);
                            System.out.println("I am at loaction:" + myLocation + " Couldn't chop trying to move to tree ID:" + TargetTree.getID() + " at location:" + TargetTree.getLocation());

                        }
                        break;
                    case 2:

                        if (rc.canShake(TargetTree.getID())) {
                            rc.shake(TargetTree.getID());
                        } else {
                            Direction toTree = myLocation.directionTo(TargetTree.getLocation());
                            tryMove(toTree);
                            break;
                        }
                        break;
                    case 0:

                        tryMove(randomDirection());
                        break;
                    default:

                        tryMove(randomDirection());
                        break;
                }



                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Lumberjack Exception");
                e.printStackTrace();
            }
        }
    }

    static void runScout() throws GameActionException {
        System.out.println("I'm an scout!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true)

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                //check for nearby trees and attempt to chop them down. ~Movian

                TreeInfo[] Trees = rc.senseNearbyTrees();
                MapLocation myLocation = rc.getLocation();

                if (Trees.length > 0){
                    MapLocation TreeLocation = Trees[0].getLocation();

                    //Tree contains a robot check if tree in range to shake.
                    for (int i = 0; i < Trees.length; i++) {
                        if (rc.canShake(Trees[i].getID())) {
                            rc.shake(Trees[i].getID());
                        }
                    }

                    Direction toTree = myLocation.directionTo(TreeLocation);
                    tryMove(toTree);


                }



                //Tree is not in range, move towards tree

                RobotInfo[] robots2 = rc.senseNearbyRobots(-1, enemy);

                // If there are some...
                if (robots2.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireSingleShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireSingleShot(rc.getLocation().directionTo(robots2[0].location));
                    }
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();



            }
            catch (Exception e){
                System.out.println("scout Exception");
                e.printStackTrace();
            }

    }
    /**
     * Returns a random Direction
     * @return a random Direction
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,3);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }



    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    static boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }
}