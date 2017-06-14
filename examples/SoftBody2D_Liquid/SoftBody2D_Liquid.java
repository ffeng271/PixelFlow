/**
 * 
 * PixelFlow | Copyright (C) 2016 Thomas Diewald - http://thomasdiewald.com
 * 
 * A Processing/Java library for high performance GPU-Computing (GLSL).
 * MIT License: https://opensource.org/licenses/MIT
 * 
 */


package SoftBody2D_Liquid;


import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwParticleFluidFX;
import com.thomasdiewald.pixelflow.java.softbodydynamics.DwPhysics;
import com.thomasdiewald.pixelflow.java.softbodydynamics.particle.DwParticle2D;

import controlP5.Accordion;
import controlP5.ControlP5;
import controlP5.Group;
import processing.core.*;
import processing.opengl.PGraphics2D;



public class SoftBody2D_Liquid extends PApplet {
  //
  // 2D Verlet Particle System.
  // 
  // + Collision Detection
  //
  //
  
  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  int gui_w = 200;
  int gui_x = 20;
  int gui_y = 20;
  
  // particle system
  ParticleSystem particlesystem;
  
  DwParticleFluidFX particle_fluid_fx;
  
  PGraphics2D pg_particles;
  
  // physics parameters
  DwPhysics.Param param_physics = new DwPhysics.Param();
  
  // verlet physics, handles the update-step
  DwPhysics<DwParticle2D> physics;
  
  // some state variables for the GUI/display
  int     BACKGROUND_COLOR    = 0;
  boolean COLLISION_DETECTION = true;
  
  
  public void settings() {
    size(viewport_w, viewport_h, P2D);
    smooth(4);
  }
  
  public void setup() {
    surface.setLocation(viewport_x, viewport_y);
    
    pg_particles = (PGraphics2D) createGraphics(width, height, P2D);
    
    // main library context
    DwPixelFlow context = new DwPixelFlow(this);
    context.print();
    context.printGL();
    
    particle_fluid_fx = new DwParticleFluidFX(context);
    
    // particle system object
    particlesystem = new ParticleSystem(this, width, height);
    
    // set some parameters
    particlesystem.PARTICLE_COUNT              = 4000;
    particlesystem.PARTICLE_SCREEN_FILL_FACTOR = 0.30f;

    particlesystem.MULT_GRAVITY                  = 0.40f;
    particlesystem.particle_param.DAMP_BOUNDS    = 0.89999f;
    particlesystem.particle_param.DAMP_COLLISION = 0.89999f;
    particlesystem.particle_param.DAMP_VELOCITY  = 0.9999f;
    
    particlesystem.initParticles();
    
    physics = new DwPhysics<DwParticle2D>(param_physics);
    param_physics.GRAVITY = new float[]{0, 0.1f};
    param_physics.bounds  = new float[]{0, 0, width, height};
    param_physics.iterations_collisions = 8;
    param_physics.iterations_springs    = 0; // no springs in this demo
   
    createGUI();

    background(0);
    frameRate(600);
  }
  
 
  public void draw() {    

    //  add force: Middle Mouse Button (MMB) -> particle[0]
    if(mousePressed){
      float[] mouse = {mouseX, mouseY};
      particlesystem.getParticleHand().moveTo(mouse, 0.3f);
      particlesystem.getParticleHand().enableCollisions(false);
    } else {
      particlesystem.getParticleHand().enableCollisions(true);
    }
    
    // update physics step
    boolean collision_detection = COLLISION_DETECTION && particlesystem.particle_param.DAMP_COLLISION != 0.0;
    
    physics.param.GRAVITY[1] = 0.05f * particlesystem.MULT_GRAVITY;
    physics.param.iterations_collisions = collision_detection ? 4 : 0;

    physics.setParticles(particlesystem.particles, particlesystem.particles.length);
    physics.update(1);

    // RENDER
    PGraphics2D pg = pg_particles;
    pg.beginDraw();
    pg.background(BACKGROUND_COLOR, 0);
    pg.blendMode(BLEND);
    particlesystem.display(pg);
    pg.blendMode(BLEND);
    pg.endDraw();
    

    // apply liquid filter
    particle_fluid_fx.apply(pg_particles);

   
    background(BACKGROUND_COLOR);
    image(pg, 0, 0);
    
    // info
    String txt_fps = String.format(getClass().getName()+ "   [size %d/%d]   [frame %d]   [fps %6.2f]", width, height, frameCount, frameRate);
    surface.setTitle(txt_fps);
  }
  


  public void activateCollisionDetection(float[] val){
    COLLISION_DETECTION = (val[0] > 0);
  }


  ControlP5 cp5;
  
  public void createGUI(){
    cp5 = new ControlP5(this);
    
    int sx, sy, px, py, oy;
    
    sx = 100; sy = 14; oy = (int)(sy*1.5f);
    
    ////////////////////////////////////////////////////////////////////////////
    // GUI - PARTICLES
    ////////////////////////////////////////////////////////////////////////////
    Group group_particles = cp5.addGroup("Particles");
    {
      
      group_particles.setHeight(20).setSize(gui_w, 200)
      .setBackgroundColor(color(16, 180)).setColorBackground(color(16, 180));
      group_particles.getCaptionLabel().align(CENTER, CENTER);
      
      sx = 100; px = 10; py = 10;oy = (int)(sy*1.4f);
      
      cp5.addButton("reset particles").setGroup(group_particles).setWidth(160).setPosition(10, 10).plugTo(particlesystem, "initParticles");

      cp5.addSlider("Particle count").setGroup(group_particles).setSize(sx, sy).setPosition(px, py+=oy+10)
          .setRange(10, 10000).setValue(particlesystem.PARTICLE_COUNT).plugTo(particlesystem, "setParticleCount");
      
      cp5.addSlider("Fill Factor").setGroup(group_particles).setSize(sx, sy).setPosition(px, py+=oy)
      .setRange(0.2f, 1.5f).setValue(particlesystem.PARTICLE_SCREEN_FILL_FACTOR).plugTo(particlesystem, "setFillFactor");
      
      cp5.addSlider("VELOCITY").setGroup(group_particles).setSize(sx, sy).setPosition(px, py+=oy+10)
          .setRange(0.85f, 1.0f).setValue(particlesystem.particle_param.DAMP_VELOCITY).plugTo(particlesystem.particle_param, "DAMP_VELOCITY");
      
      cp5.addSlider("GRAVITY").setGroup(group_particles).setSize(sx, sy).setPosition(px, py+=oy)
          .setRange(0, 10f).setValue(particlesystem.MULT_GRAVITY).plugTo(particlesystem, "MULT_GRAVITY");

      
      cp5.addSlider("SPRINGINESS").setGroup(group_particles).setSize(sx, sy).setPosition(px, py+=oy)
          .setRange(0, 1f).setValue(particlesystem.particle_param.DAMP_COLLISION).plugTo(particlesystem.particle_param, "DAMP_COLLISION");
      
      cp5.addCheckBox("activateCollisionDetection").setGroup(group_particles).setSize(40, 18).setPosition(px, py+=(int)(oy*1.5f))
          .setItemsPerRow(1).setSpacingColumn(3).setSpacingRow(3)
          .addItem("collision detection", 0)
          .activate(COLLISION_DETECTION ? 0 : 2);
          }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // GUI - DISPLAY
    ////////////////////////////////////////////////////////////////////////////
    Group group_display = cp5.addGroup("display");
    {
      group_display.setHeight(20).setSize(gui_w, 25)
      .setBackgroundColor(color(16, 180)).setColorBackground(color(16, 180));
      group_display.getCaptionLabel().align(CENTER, CENTER);
      
      px = 10; py = 15;
      
      cp5.addSlider("BACKGROUND").setGroup(group_display).setSize(sx,sy).setPosition(px, py)
          .setRange(0, 255).setValue(BACKGROUND_COLOR).plugTo(this, "BACKGROUND_COLOR");
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // GUI - ACCORDION
    ////////////////////////////////////////////////////////////////////////////
    cp5.addAccordion("acc").setPosition(gui_x, gui_y).setWidth(gui_w).setSize(gui_w, height)
      .setCollapseMode(Accordion.MULTI)
      .addItem(group_particles)
      .addItem(group_display)
      .open(0);
  }
  
   


  public static void main(String args[]) {
    PApplet.main(new String[] { SoftBody2D_Liquid.class.getName() });
  }
}