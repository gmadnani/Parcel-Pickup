# Parcel-Pickup

## Overview
Robotic Mailing Solutions Inc. (RMS) were so happy with your changes and design report for the Automail
simulation system they decided to bring you onboard for a more challenging task. Wanting to break out
of the bounds of delivering only within high rise buildings, RMS have decided to jump on the autonomous
vehicle bandwagon and start a parcel delivery service. Right from the start they recognised that if they
want to go global with their solution they will need to support different approaches that recognise local
conditions.
RMS has provided you with a simple environment to allow you to demonstrate your design and development
skills in controlling a parcel pickup vehicle with software-interfacing sensors and actuators. With
your tailor-designed flexible integrated vehicle auto-drive and parcel pickup subsystem, this vehicle will
navigate the world to locate and retrieve parcels, then take them to the RMS delivery depot.

## The World
The area you find yourself in is constructed in the form of a simple grid of tiles consisting only of:
* Roads (some roads may lead to dead-ends)
* Walls
* Parcels
* Start
* Exit

Each environment only has one start and one exit, and the exit can only be used once a sufficient
number of parcels have been collected.

## The Vehicle
The car you find yourself in includes a range of sensors for detecting the car’s speed and direction, and
actuators for accelerating, braking and turning. It has only one speed (which can be applied in both
forward and reverse) and can only turn towards one of the four compass points. The car can only turn when
it is already moving forwards or backwards. The car also includes a short-range GPS that can detect/see
four tiles in all directions (that’s right, it can see through walls) – that is, it can detect the kinds of tiles in
a 9x9 area centered on the car.
The car has a limited amount of health, and a limited amount of fuel. One unit of fuel is consumed for
each tile the car moves onto, and the car is damaged (i.e., loses health) if it is driven into a wall. There is
currently no way to regain health or fuel, but there may be in the future. If the car’s health or fuel become
zero, your software fails the test. If, however, your software takes the car from the start to the exit, picking
up at least the number of required parcels on the way, your software passes the test.

## The Task
Your task is to design, implement, integrate, and test a car auto-controller that is able to successfully
traverse a map. It must be capable of safely:
1. exploring the map and locating the parcels,
2. retrieving the required number of parcels (or more), and
3. making its way to the exit.

It should complete these tasks before the car runs out of either health or fuel. At this stage RMS are not
looking for complex or optimal algorithms for completing these tasks, but the solution you design should
be well thought out and open for future extensions (e.g., to deal with hazards in the environment, or the
make use of additional features in the delivery vehicle). You will not be assessed on how fast-traversing
your algorithms are, just on the design in which they are embedded and on whether they work to get the
car to the exit with an appropriate number of packages.