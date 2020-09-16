package com.caibixzy.ledmatrixctrl;

import processing.core.*;

//import processing.serial.*;
//import processing.net.*;
//import ddf.minim.*;
//import ddf.minim.analysis.*;

//import javax.sound.sampled.*;

public class sketch_191214b extends PApplet {

    final int cGUIRefreshFPS = 30;
    PFont font, generatedFont;
    final int cDefaultOutputFPS = 30;
    MatrixObj matrix;
    byte[] TransmissionArray = new byte[1]; //packs the data to be sent out into here, it sends it faster if they are bytes
    short[] PatchCoordX = new short[1];
    short[] PatchCoordY = new short[1];
    int displayPixSize = 10; //start at default, recalculated to new size when patch file is loaded
    int displayOffsetX = 0;
    int displayOffsetY = 0;
    final int cPreviewContentWidth = 450;
    final int cPreviewContentHeight = 260;
    int layer0Holdmills = 0;
    int layer1Holdmills = 0;
    int tile0PlayMode = 0;
    int tile1PlayMode = 0;
    int layer0Opacity = 200;
    int layer1Opacity = 200;
    int tile0ScaleOption = 0;
    int tile1ScaleOption = 0;
    int layer0EffectFilterIDNum = 0;
    int layer1EffectFilterIDNum = 0;
    int layer0effectFilterVariable = 0;
    int layer1effectFilterVariable = 0;
    int layer0BlendMode = 0;
    int layer1BlendMode = 0;
    final int cDefaultContentFPS = 24;
    float FeedIntensityA = 0.1f;
    boolean mixFeeds = false;
    boolean isSetup = false;
    int holdMillisDraw = millis();
    PImage transmitPixelBuffer, feedPreviewImgA;
    final int cBlendID[] = {BLEND, ADD, SUBTRACT, DARKEST, LIGHTEST, DIFFERENCE, EXCLUSION, MULTIPLY, SCREEN, REPLACE}; //needed to call blendMode(variable)
    final int cFilterIDStr[] = {9999, THRESHOLD, GRAY, OPAQUE, INVERT, POSTERIZE, BLUR, ERODE, DILATE};
    PGraphics LayerContentGBufA, MixedContentGBuf; //final location of the mixed layers
    PGraphics scratchGBuf; //used for generated content
    PImage scratchImg; // Image to receive a texture and scratch

    guiTextField text = new guiTextField("test",16,0,80,80,200,200,1,10,100,true,false,"test");
    PImage mediaImage0 = createImage(1, 1, ARGB); //ARGB since it is an image, PGraphics are always ARGB
    PImage mediaImage1 = createImage(1, 1, ARGB); //ARGB since it is an image, PGraphics are always ARGB
    generatedPlasma genContentPlasma;
    generatedText genContentText;
    generatedFallingBlocks genContentBlocks;

    public void settings()
    {
        // Initial window size
        //String[] workString = new String[5]; //used to divide the lines into tab
        //String[] strLines = loadStrings("software.ini"); //divides the lines
        //software.ini now loaded
        //workString = split(strLines[1], '\t'); //get GUIWIDTH value
        //int tempW = PApplet.parseInt(workString[1]);
        //workString = split(strLines[2], '\t'); //get GUIHEIGHT value
        //size(tempW, PApplet.parseInt(workString[1]), P3D); //size window
        size(720, 260, P3D); //P3D is required, but has some issues

        //PImage titlebaricon = loadImage("favicon.gif"); //doesn't work with P3D it seems
        //surface.setIcon(titlebaricon);
    }


    public void setup() {
        //without this P3D renderer will draw all strokes last, so they overlay all other drawing
        hint(DISABLE_OPTIMIZED_STROKE); //Significantly slows down the GUI

        //surface.setResizable(true);   // Needed for resizing the window to the sender size
        //surface.setLocation(100, 100);
        //surface.setTitle("NLED AllPixMatrix - Northern Lights Electronic Design, LLC");

        colorMode(RGB);
        frameRate(cGUIRefreshFPS); //set to any default, only affects the GUI, does not affect media or output rates


        font = createFont("Arial-BoldMT", 48);
        generatedFont = loadFont("Arial-BoldMT-48.vlw"); //for use with the text generated media, edit with your own if you want
        textFont(font);
        matrix = new MatrixObj();
        matrix.loadPatchFile();

        LayerContentGBufA =  createGraphics(matrix.width, matrix.height);
        MixedContentGBuf =  createGraphics(matrix.width, matrix.height);
        scratchGBuf = createGraphics(matrix.width, matrix.height);
        scratchImg = createImage(matrix.width, matrix.height, ARGB);

        genContentPlasma = new generatedPlasma();
        genContentText = new generatedText();
        genContentBlocks = new generatedFallingBlocks();

        MainMixFunction(); //update right away to prevent errors
        isSetup = true;

        //thread("OutputTransmissionThread"); //START TRANSMISSION THREAD
        //thread("MainMixingThread"); //updates the media that is assigned to the layers
    } //end setup()

    public void draw()
    {
        background(200);
        noStroke();
        fill(50); //black
        rect(0, 0, 450, 260); //left preview background
        //rect(455, 0, 450, 260); //center preview background
        //rect(910, 0, 450, 260); //right preview background

        //feedPreviewImgA = LayerContentGBufA.get(); //quicker to convert the PGraphics to PImage before running .get() it seems,
        //feedPreviewImgB = LayerContentGBufB.get(); //distinctly noticable when fading back and fourth

        //Feed A preview
        for (int i = 0; i < feedPreviewImgA.width; i++)
        {
            for (int j = 0; j < feedPreviewImgA.height; j++)
            {
                int pix = feedPreviewImgA.get(i, j);
                fill(pix);
                rect(10+displayOffsetX+(i*displayPixSize), 10+displayOffsetY+(j*displayPixSize), displayPixSize, displayPixSize);
            }
        }

        //genContentText.buildFrame();

        //mediaImage0 = genContentText.localPGBuf.get();
        //genContentPlasma.buildFrame();
        //mediaImage0 = genContentPlasma.localPGBuf.get();
        /*
        genContentBlocks.buildFrame();
        mediaImage0 = genContentBlocks.localPGBuf.get();
        fill(0); //black
        rect(0, 0, 450, 260);
        if (mediaImage0 != null)
        {
            try {
                image(mediaImage0, 0, 0, 450, 260); //stretches it to fit
            }
            catch(Exception e) {
                println("An error happened with content tile: ");
            }
        }
        */
        //text.display();

        fill(0);
        textSize(18);

        text("GUI Rate(FPS): "+frameRate, 500, 130);
        //text("Feed A Intensity: "+(100*FeedIntensityA)+"%", 540, 445);
        //text("Feed B Intensity: "+(100*FeedIntensityB)+"%", 540, 465);

    } //end draw()

    class guiTextField
    {
        String label;
        int xpos; // rect xposition
        int ypos ; // rect yposition
        int bWidth;
        int bHeight;
        int colBG;
        int colHL;

        int inputMethod; //0 = characters only, 1 = numbers only , 2 = numbers only with trailing %, 3 = floats
        int minValue; //either min number, or minim characters
        int maxValue; //either max number, or maximum characters
        boolean toggle;
        boolean selected;
        String callBack;

        int status; //0=show, 1=grey out, 2=hide

        boolean MouseOverFlag;

        guiTextField(String iLabel, int ixpos, int iypos, int ibWidth, int ibHeight, int iColBG, int iColHL, int iInputMethod, int iminValue, int imaxValue, boolean iToggle, boolean iSelected, String itxtfCallback)
        {
            label = iLabel;
            xpos = ixpos;
            ypos = iypos;
            bWidth = ibWidth;
            bHeight = ibHeight;
            colBG = iColBG;
            colHL = iColHL;
            toggle = iToggle;
            selected = iSelected;
            inputMethod = iInputMethod;
            minValue = iminValue;
            maxValue = imaxValue;
            callBack = itxtfCallback;
            status = 0; //init at show
            MouseOverFlag = false;
        }

        public void display()
        {
            if (status == 2) return; //element is hidden, do not draw

            //translate(0, 0);
            strokeWeight(2);
            fill(colBG);

            if (selected==true)
            {
                stroke(colHL);
            } else
            {
                //stroke(gui.buttonColor);
                if (status == 0 && MouseOverFlag == true)
                {
                    stroke(colHL, 128);
                    fill(colBG, 192);
                }
            }
            rect(xpos, ypos, bWidth, bHeight);

            //adjust stroke to on higligh and invert text color
            noStroke();
            fill(0);
            textSize((bHeight/1.75f));

            textAlign(LEFT);
            if (inputMethod != 2) text(label, xpos+((bWidth - textWidth(label))/2), ypos+((bHeight-(textAscent()+textDescent()))/1.5f), bWidth, bHeight);
            else  text(label+"%", xpos+((bWidth - textWidth(label+1))/2), ypos+((bHeight-(textAscent()+textDescent()))/1.5f), bWidth, bHeight);

            if (toggle==false) selected = false;
        }
    }

    class generatedText
    {
        PGraphics localPGBuf;

        //parameters
        String textLabel;
        int textSize;
        int xOffset;
        int yOffset;
        int scrollingType;
        int fillColor;
        int bgColor;

        //local variables
        int scrollVal;

        generatedText()
        {
            localPGBuf = createGraphics(matrix.width, matrix.height); //Do not use P2D, that causes smoothing issues

            //parameters
            textLabel = "Your Text";
            textSize = 16; //set it in the number input field for now, or set to a static
            xOffset = yOffset = 0;
            scrollingType = 1; //Stationary
            fillColor = color(255);
            bgColor = color(0, 0, 0, 0); //start transparent - had to make one color a 1 or it wouldn't init transparent, had to reapply bg color

            //local variables
            scrollVal = 0;

            //init code
        }

        public void buildFrame()
        {
            //localPGBuf.noSmooth();
            localPGBuf.beginDraw();
            localPGBuf.background(bgColor);
            localPGBuf.textAlign(LEFT, TOP);
            localPGBuf.textFont(generatedFont); //if not decalared, it uses a font that is sized for the initial value of genNIFTextSize, which makes it blurry
            localPGBuf.textSize(textSize);
            localPGBuf.fill(fillColor); //colors it white for now

            switch(scrollingType)
            {
                case 0: //stationary
                    localPGBuf.text(textLabel, xOffset, yOffset);
                    break;
                case 1: //left
                    scrollVal++;
                    if (scrollVal > (textWidth(textLabel))) scrollVal = -matrix.width;
                    localPGBuf.text(textLabel, xOffset-scrollVal, yOffset);
                    break;
                case 2: //right
                    scrollVal++;
                    if (scrollVal > matrix.width) scrollVal = -PApplet.parseInt(textWidth(textLabel)+matrix.width);
                    localPGBuf.text(textLabel, xOffset+scrollVal, yOffset);
                    break;
                case 3: //up
                    scrollVal++;
                    if (scrollVal > (textSize+yOffset)) scrollVal = -matrix.height;
                    localPGBuf.text(textLabel, xOffset, yOffset-scrollVal);
                    break;
                case 4: //down
                    scrollVal++;
                    if (scrollVal > (matrix.height)) scrollVal = -textSize;
                    localPGBuf.text(textLabel, xOffset, yOffset+scrollVal);
                    break;
            }

            localPGBuf.endDraw();
        }
    } //end class

    class generatedFallingBlocks
    {
        PGraphics localPGBuf;

        //parameters
        int size;
        int frequency; //lower is faster
        float decay;
        int direction; //0- fall down, 1 - fall up, 2 - fall right, 3 - fall left
        int fillColor;
        //might be cool to be able to time change direction for zig-zags and such

        //local variables
        int[] fallingBlocksArray;
        int largestDimension;

        generatedFallingBlocks()
        {
            localPGBuf = createGraphics(matrix.width, matrix.height); //ARGB - used for generating content

            //local values

            //initial parameters
            size = 1;
            frequency = 5;
            decay = 0.75f;
            direction = 0;
            fillColor = color(255, 255, 255, 255); //default white

            //init code

            //do it this way or do it the right way of selecting width or height based on direction
            largestDimension = max(matrix.width, matrix.height);

            //randomly fill stating up to largestDimension
            fallingBlocksArray = new int[largestDimension];
            for (int i = 0; i != largestDimension; i++)   fallingBlocksArray[i] = (int)random(0, largestDimension*2);
        }

        public void buildFrame()
        {
            localPGBuf.beginDraw();
            localPGBuf.rectMode(CORNER);
            localPGBuf.noStroke();

            DecayBuffer(decay, localPGBuf); //do this here and not earlier apparently, maybe colorMode? not sure

            localPGBuf.fill(fillColor);
            //could find a way to make the trails and the blocks different colors

            switch(direction)
            {
                case 0: //falling down
                    for (int i = 0; i != matrix.width; i++)
                    {
                        fallingBlocksArray[i]++;
                        if (fallingBlocksArray[i] > matrix.height) fallingBlocksArray[i] =  (int)random(-frequency, -1);
                        localPGBuf.rect(i, fallingBlocksArray[i], size, size);
                    } //for()
                    break;
                case 1: //falling up
                    for (int i = 0; i != matrix.width; i++)
                    {
                        fallingBlocksArray[i]--;
                        if (fallingBlocksArray[i] < 0) fallingBlocksArray[i] =  (int)random(frequency+matrix.height, matrix.height+1);
                        localPGBuf.rect(i, fallingBlocksArray[i], size, size);
                    } //end for()
                    break;
                case 2: //falling right
                    for (int i = 0; i != matrix.height; i++)
                    {
                        fallingBlocksArray[i]++;
                        if (fallingBlocksArray[i] > matrix.width) fallingBlocksArray[i] =  (int)random(-frequency, -1);
                        localPGBuf.rect(fallingBlocksArray[i], i, size, size);
                    } //for()
                    break;
                case 3: //falling left
                    for (int i = 0; i != matrix.height; i++)
                    {
                        fallingBlocksArray[i]--;
                        if (fallingBlocksArray[i] < 0) fallingBlocksArray[i] =  (int)random(frequency, matrix.width-1); //if off screen, re-randomize
                        localPGBuf.rect(fallingBlocksArray[i], i, size, size);
                    } //end for()
                    break;
            } //end switch

            localPGBuf.endDraw();
            //println("finished");
        }
    } //end class

    public void DecayBuffer(float passedValue, PGraphics buf)
    {
        //println("DecayBuffer() with "+passedValue);
        //multi use function for decay/trail effect
        //really don't like how this works, but is fine for now - any ideas?

        int myColor;
        int myRed, myGreen, myBlue;

        if (passedValue == 1) return; //leave if no decay is too be applied

        buf.loadPixels();

        for (int i = 0; i != buf.pixels.length; i++) //decay/ fade out entire pixel[] array
        {
            myColor = buf.pixels[i];

            myRed = myColor >> 16 & 0xFF; //convert colors to 8-bit
            myGreen = myColor >> 8 & 0xFF;
            myBlue = myColor & 0xFF;

            //if almost black, just make it black and transparent
            if (floor(myRed*passedValue) < 2 && floor(myGreen*passedValue) < 2 && floor(myBlue*passedValue) < 2)  //buf.pixels[i] &= 0x00FFFFFF;
                buf.pixels[i] = color(0, 0, 0, 0);
            else
                buf.pixels[i] = color(floor(myRed*passedValue), floor(myGreen*passedValue), floor(myBlue*passedValue), floor(255*passedValue));
        }
        buf.updatePixels();
    }

    class generatedPlasma
    {
        //doesn't work well on large matrices, also colors are not controllable. Would like to replace how this works.
        PGraphics localPGBuf;

        //parameters


        //local variables
        int[][] waves = new int[1000][3]; // sine waves
        int[] luma = new int[1024]; // brightness curve
        int[][][] pos = new int[3][2][3]; // positions RGB,XY,123
        float[][][] velocity = new float[3][2][3]; // velocity RGB,XY,123
        int[][][] wavesB = new int[300][3][2]; // pos,RGB,XY
        int ix;
        int iy;
        int iz;
        int xOff;

        // int largestDimension;

        generatedPlasma()
        {
            localPGBuf = createGraphics(matrix.width, matrix.height, P2D); //ARGB - used for generating content

            //parameters

            //local values
            /* OpenProcessing variation of *@*http://www.openprocessing.org/sketch/7035*@* */
            /* !do not delete the line above, required for linking your modification if you re-upload */
            // PLASMA - Will Birtchnel 2010

            // randomize positions
            for (ix=0; ix<3; ix++) {
                for (iy=0; iy<2; iy++) {
                    for (iz=0; iz<3; iz++) {
                        pos[iz][iy][ix]=PApplet.parseInt(random(512));
                        velocity[iz][iy][ix]=random(-3, 3);
                    }
                }
            }

            // make sine waves
            // largestDimension = max(matrix.width, matrix.height); //was just MatrixWidth, not sure if this is best

            for (ix=0; ix<100; ix++) {
                waves[ix][0]= (int)(100+ (sin(ix*TWO_PI/matrix.width)*100));
                waves[ix][1]= (int)(50+  (sin(ix*2*TWO_PI/matrix.width)*50));
                waves[ix][2]= (int)(25+  (sin(ix*3*TWO_PI/matrix.width)*25));
            }

            // make luma wave
            for (ix=0; ix<1024; ix++) {
                iy=ix;
                while ( (iy>255)||(iy<0)) {
                    if (iy>255) iy=511-iy;
                    if (iy<0) iy=abs(iy);
                }
                if (iy>201) iy=((iy*4)-(201*3));
                iy=(int)((iy*255)/((255*4)-(201*3)));
                luma[ix]=iy;
            }

            //init code
        } //end declaration


        public String saveParameters()
        {
            //none
            return "NONE";
        }

        public void loadParameters(String passedStr)
        {
            //none
        }

        public void buildFrame()
        {

            localPGBuf.beginDraw();
            // localPGBuf.colorMode(ARGB, 255); //required or wierd things happen

            // PLASMA - Will Birtchnel 2010
            //From http://www.openprocessing.org/sketch/7035
            // update velocity
            for (ix=0; ix<3; ix++) {
                for (iy=0; iy<2; iy++) {
                    for (iz=0; iz<3; iz++) {
                        velocity[iz][iy][ix]+=random(-0.1f, 0.1f);
                        if (velocity[iz][iy][ix]>(3-ix)) velocity[iz][iy][ix]=(3-ix);
                        if (velocity[iz][iy][ix]<(-3+ix)) velocity[iz][iy][ix]=(-3+ix);
                    }
                }
            }
            // update positions
            for (ix=0; ix<3; ix++) {
                for (iy=0; iy<2; iy++) {
                    for (iz=0; iz<3; iz++) {
                        pos[iz][iy][ix]+=PApplet.parseInt(velocity[iz][iy][ix]);
                        if (pos[iz][iy][ix]>=matrix.width) pos[iz][iy][ix]-=matrix.width*2;
                        if (pos[iz][iy][ix]<0) pos[iz][iy][ix]+=matrix.width*2;
                    }
                }
            }
            // make composite waves
            for (ix=0; ix<matrix.width; ix++) {
                for (iy=0; iy<3; iy++) {
                    for (iz=0; iz<2; iz++) {
                        wavesB[ix][iy][iz]= waves[ix+pos[iy][iz][0]][0]  + waves[ix+pos[iy][iz][1]][1] + waves[ix+pos[iy][iz][2]][2];
                    }
                }
            }

            //draw pixels
            localPGBuf.loadPixels();

            for (iy = 0; iy < matrix.height; iy++)
            {
                xOff = iy*matrix.width;
                for (ix = 0; ix < matrix.width; ix++)
                {
                    localPGBuf.pixels[xOff+ix]= ((luma[wavesB[ix][0][0]+wavesB[iy][0][1]]<<16)+(luma[wavesB[ix][1][0]+wavesB[iy][1][1]]<<8)+luma[wavesB[ix][2][0]+wavesB[iy][2][1]]) | 0xFF000000;
                }
            }
            localPGBuf.updatePixels();

            localPGBuf.endDraw();
        }
    } // /end class

    class MatrixObj
    {
        String name;
        String patchFileName;
        String contentFileName;
        String footagePath;
        String automaticFileName;

        int patchedChannels;
        int totalPixels;

        boolean auroraCMD;

        int transmissionType; //0: none, 1: NLED serial, 2: glediator serial, 3: ArtNet, 4: ??

        int serialPortNum; //ID number is assigned by operating system, and may change if other serial devices are connected
        int serialBaud;
        //add more serial ports

        //Network Ports
        String outputNetworkIPAdr;
        int outputNetworkPort;

        int width;
        int height;
        int colorOrderID; //0: RGB, 1: BRG, 2: GBR, 3: RBG, 4: BGR, 5: GRB



        int outputFPS; //in milliseconds

        boolean externalDataEnable;
        boolean externalDataRunning;
        int externalDataPort;
        int externalDataBaud;

        //--------------------------------------------------------------------------

        MatrixObj()
        {
            outputFPS = 1000/cDefaultOutputFPS; //convert FPS to milliseconds
        }

        //--------------------------------------------------------------------------

        public void loadPatchFile()
        {
            //loads a coordinate patch file, find min and mix point in both directions and scales the overall size to fit within the preview areas
            // creates the Pixel objects to display during draw and fills the Coordinate Arrays for Pixel Patching
            println("loadPatchFile()");

            int tempTransArraySize = 0;
            int Xdifference = 0;
            int Ydifference = 0;

            String[] lines = loadStrings("configs/16x16-square/16x16-patch.txt"); //divides the lines
            String[] WorkString = new String[3]; //used to divide the lines into tab

            WorkString = split(lines[0], '\t');
            matrix.totalPixels = PApplet.parseInt(WorkString[0]);

            //---------------------------------------------------------------------------------------------------------

            switch(matrix.colorOrderID)
            {
                case 0: //RGB
                case 1: //BRG
                case 2: //GBR
                case 3: //RBG
                case 4: //BGR
                case 5: //GRB
                    matrix.patchedChannels = matrix.totalPixels *3; //not the same as TotalChannels, incase of non-square matrixes
                    tempTransArraySize = matrix.patchedChannels;
                    break;

                case 6: //RGBW - no way
                case 7: //GRBW  - no way
                    //would be *4
                    break;

                case 8: //Single Color - probably not

                    break;
            }

            println("matrix.patchedChannels: "+matrix.patchedChannels);
            println("matrix.totalPixels: "+matrix.totalPixels);

            //resizes arrays to match the amount
            TransmissionArray = new byte[1]; //must resize the Array so it is exactly the same size as channels
            TransmissionArray = expand(TransmissionArray, tempTransArraySize);//matrix.patchedChannels);

            int MinX=10000; //set to a large value
            int MinY=10000;
            int MaxX=0;
            int MaxY=0;

            for (int i=1; i != lines.length; i++)
            {
                WorkString = split(lines[i], '\t');
                if (PApplet.parseInt(WorkString[0]) > MaxX) MaxX = PApplet.parseInt(WorkString[0]);
                if (PApplet.parseInt(WorkString[0]) < MinX) MinX = PApplet.parseInt(WorkString[0]);

                if (PApplet.parseInt(WorkString[1]) > MaxY) MaxY = PApplet.parseInt(WorkString[1]);
                if (PApplet.parseInt(WorkString[1]) < MinY) MinY = PApplet.parseInt(WorkString[1]);
            }
            // println("X: "+MinX+" : "+MaxX);
            // println("Y: "+MinY+" : "+MaxY);

            Xdifference = MaxX - MinX;
            Ydifference = MaxY - MinY;
            //  println(Xdifference+" : "+Ydifference);

            PatchCoordX = new short[matrix.patchedChannels]; //resize the patch arrays based on matrix.patchedChannels
            PatchCoordY = new short[matrix.patchedChannels];

            //file created was incremented method channel numbers
            for (int i=0; i != matrix.totalPixels; i++)
            {
                WorkString = split(lines[i+1], '\t');
                PatchCoordX[i] = (short)(PApplet.parseInt(WorkString[0]) - MinX);
                PatchCoordY[i] = (short)(PApplet.parseInt(WorkString[1]) - MinY);
            }

            //set final matrix size
            matrix.width = Xdifference+1;//not base 0
            matrix.height = Ydifference+1;// not base 0

            //println("maxX: "+MaxX+"   maxY: "+MaxY);

            //set pixSize that draw() uses to draw the matrix preview
            if (MaxX > MaxY)
            {
                displayPixSize = ((cPreviewContentWidth-20) / MaxX);

            }
            else
            {
                displayPixSize = ((cPreviewContentHeight-20)  / MaxY);

            }
            //calculate offsets to center pixel preview grids
            displayOffsetX = (((cPreviewContentWidth-20) - (displayPixSize*MaxX)) / 2);
            displayOffsetY = (((cPreviewContentHeight-20)  - (displayPixSize*MaxY)) / 2);



            println("Done loading patch");
        }//end method
    } //end obje

    public void MainMixFunction()
    {
        //only call from draw() otherwise creates graphical glitches
        //This function uses a PImage or PGraphics buffer for both Feeds(A&B), the mixed buffer. A scratch buffer is used to copy the
        //	the mediaTile's graphic buffer, the media images are layered an to create the mixed layer image.
        //	The mixed layer image

        // --------------------------------------------------- Mix Side A ------------------------------------------------------------------
        //----------------------------------------- Feed A, Bottom Layer ----------------------------------------------------

        //contentLayerA[0].updateLayerFrame();
        scratchImg = mediaImage0.get();

        scratchGBuf.beginDraw();
        scratchGBuf.background(0, 0, 0, 0); //required... for now anyway, needed for tint to work
        scratchGBuf.tint(255, layer0Opacity);  //Opacity 3rd Layery
        MixingCommonScaleFunc(tile0ScaleOption);
        MixingCommonApplyEffects(layer0EffectFilterIDNum, layer0effectFilterVariable);
        scratchGBuf.endDraw();
        //MixingCommonApplyColorEffects(contentLayerA[0]);


        LayerContentGBufA.beginDraw();
        LayerContentGBufA.background(0, 0, 0, 0); //Clear before (required for tint())
        LayerContentGBufA.blendMode(cBlendID[layer0BlendMode]);

        LayerContentGBufA.image(scratchGBuf, 0, 0, scratchGBuf.width, scratchGBuf.height); //display content image
        LayerContentGBufA.endDraw();

        //--------------------------------------------- Feed A, Middle Layer ------------------------------------------------

        //contentLayerA[1].updateLayerFrame();
        scratchImg = mediaImage1.get();

        scratchGBuf.beginDraw();
        scratchGBuf.background(0, 0, 0, 0); //required... for now anyway, needed for tint to work
        scratchGBuf.tint(255, layer1Opacity);  //Opacity 2nd Layer
        MixingCommonScaleFunc(tile1ScaleOption);
        MixingCommonApplyEffects(layer1EffectFilterIDNum, layer1effectFilterVariable);
        scratchGBuf.endDraw();
        //MixingCommonApplyColorEffects(contentLayerA[1]);


        LayerContentGBufA.beginDraw();
        LayerContentGBufA.blendMode(cBlendID[layer1BlendMode]);
        LayerContentGBufA.image(scratchGBuf, 0, 0, scratchGBuf.width, scratchGBuf.height); //display content image
        LayerContentGBufA.endDraw();
        /*
        MixedContentGBuf.beginDraw();
        MixedContentGBuf.background(0, 0, 0, 0); //clear graphics buffer
        MixedContentGBuf.noTint();
        MixedContentGBuf.tint(255, 255);
        MixedContentGBuf.image(GraphicsBufferOpacity(FeedIntensityA, LayerContentGBufA), 0, 0);
        MixedContentGBuf.endDraw();
        */
    } //end func

//===============================================================================================================================

    //scales the image according to the set value
    public void MixingCommonScaleFunc(int passedIDNum)
    {
        if(scratchImg.width > 0) {
            switch (passedIDNum) {
                case 0:
                    scratchGBuf.image(scratchImg, 0, 0); //display content image - no scaling or anything
                    break;
                case 1: //scale
                    scratchGBuf.image(scratchImg, 0, 0, scratchGBuf.width, scratchGBuf.height); //display content image
                    break;
                case 2: //scale W
                    scratchGBuf.image(scratchImg, 0, 0, scratchGBuf.width, ((float) scratchGBuf.width / scratchImg.width) * scratchImg.height); //display content image
                    break;
                case 3: //scale H
                    scratchGBuf.image(scratchImg, 0, 0, ((float) scratchGBuf.height / scratchImg.height) * scratchImg.width, scratchGBuf.height); //display content image
                    break;
            }
        }
    }//end func
/*
//===============================================================================================================================

    //Applies the color max and color min effects, along with contrast
    public void MixingCommonApplyColorEffects(guiContentLayer passedObj)
    {
        int myRed, myGreen, myBlue;
        int minR, minG, minB;
        int maxR, maxG, maxB;

        minR = (int)red(passedObj.minColor);
        minG = (int)green(passedObj.minColor);
        minB = (int)blue(passedObj.minColor);
        maxR = (int)red(passedObj.maxColor);
        maxG = (int)green(passedObj.maxColor);
        maxB = (int)blue(passedObj.maxColor);

        if (minR < passedObj.effectContrast) minR = passedObj.effectContrast;
        if (minG < passedObj.effectContrast) minG = passedObj.effectContrast;
        if (minB < passedObj.effectContrast) minB = passedObj.effectContrast;


        //Checks to see if running the min/max function is required - as of v.1b causes random GUI glitches, does not affect the output
        if(minR > 0 || minG > 0 || minG > 0 || maxR < 255 || maxG < 255 || maxB < 255)
        {
            //As of v.1b - This block of code causes the GUI graphical glitches --------------------------------------------
            //This goes through every pixel, and checks it is over the effects m color and over max color
            //Essentially this filters colors not within the range and sets them transparent
            scratchGBuf.loadPixels();
            for (int i = 0; i != scratchGBuf.pixels.length; i++) //decay/ fade out entire pixel[] array
            {
                myRed = scratchGBuf.pixels[i] >> 16 & 0xFF; //convert colors to 8-bit
                if (myRed < minR) myRed = 0;
                else if (myRed > maxR) myRed = maxR;

                myGreen = scratchGBuf.pixels[i] >> 8 & 0xFF;
                if (myGreen < minG) myGreen = 0;
                else if (myGreen > maxG) myGreen = maxG;

                myBlue = scratchGBuf.pixels[i] & 0xFF;
                if (myBlue < minB) myBlue = 0;
                else if (myBlue > maxB) myBlue = maxB;

                scratchGBuf.pixels[i] = color(myRed, myGreen, myBlue, alpha(scratchGBuf.pixels[i])); //THIS LINE IS CULPRIT, if removed there is no glitch
            }
            scratchGBuf.updatePixels();
            //End Graphic Glitches issue -----------------------------------------------------------------------
        }

        //overlay color tint
        if(passedObj.layerOpacity > 0 && alpha(passedObj.tintColor) > 0) //don't bother running if either opacity/alpha is already 0
        {
            scratchGBuf.beginDraw();
            //scale the tint color alpha channel with layerOpacity
            scratchGBuf.fill(red(passedObj.tintColor),green(passedObj.tintColor),blue(passedObj.tintColor),((float)passedObj.layerOpacity/255)*alpha(passedObj.tintColor));
            //now draw a rect with proper opacity over the buffer
            scratchGBuf.noStroke();
            scratchGBuf.rect(0,0,matrix.width,matrix.height);
            scratchGBuf.endDraw();
        }
    } //end func
*/
//===============================================================================================================================

    public void MixingCommonApplyEffects(int passedIDNum, float passedParameter1)
    {
        try {
            //APPLY EFFECTS BEFORE AND AFTER... Filter() after others before...
            if (cFilterIDStr[passedIDNum] == POSTERIZE || cFilterIDStr[passedIDNum] == THRESHOLD || cFilterIDStr[passedIDNum] == BLUR )
                scratchGBuf.filter(cFilterIDStr[passedIDNum], passedParameter1);
            else scratchGBuf.filter(cFilterIDStr[passedIDNum]);
        }
        catch(Exception e) {
        }
    }//end func

    //returns a PImage with opacity applied, tint() wasn't going to work for this. Also did not want to alter the original buffers or the preview would be wrong
    public PImage GraphicsBufferOpacity(float passedValue, PGraphics buf)
    {
        // println("DecayBuffer() with "+passedValue);

        //multi use function for decay/trail effect
        int myColor;
        int myRed, myGreen, myBlue;
        float alpha;

        PImage localBuf = buf.get();

        if (passedValue == 1) return buf.get(); //leave if no decay is too be applied

        localBuf.loadPixels();

        for (int i = 0; i != localBuf.pixels.length; i++) //decay/ fade out entire pixel[] array
        {
            myColor = localBuf.pixels[i];
            alpha = alpha(myColor);

            myRed = myColor >> 16 & 0xFF; //convert colors to 8-bit
            myGreen = myColor >> 8 & 0xFF;
            myBlue = myColor & 0xFF;

            localBuf.pixels[i] = color(round(myRed*passedValue), round(myGreen*passedValue), round(myBlue*passedValue), alpha);
        }
        localBuf.updatePixels();
        return localBuf.get();
    }

    public void MainMixingThread()
    {
        while(true)
        {
            //------------------------------------------------ Feed A Content Update ----------------------------------------------------------------

            try {
                if ((millis()-layer0Holdmills) > cDefaultContentFPS)
                {
                    if (tile0PlayMode == 0)
                    {
                        layer0Holdmills = millis();
                        genContentText.buildFrame();
                        mediaImage0 = genContentText.localPGBuf.get();
                        //genContentBlocks.buildFrame();
                        //mediaImage = genContentBlocks.localPGBuf.get();
                        mixFeeds = true;
                    }
                }
                if ((millis()-layer1Holdmills) > cDefaultContentFPS)
                {
                    layer1Holdmills = millis();
                    //genContentBlocks.buildFrame();
                    //mediaImage1 = genContentBlocks.localPGBuf.get();
                    //genContentPlasma.buildFrame();
                    //mediaImage0 = genContentPlasma.localPGBuf.get();
                    mixFeeds = true;
                }
            }
            catch(Exception e) {
                println("FeedA had an error");
            }
		//------------------------------------------------ Feeds Updated, Now Mix Them ----------------------------------------------------------------

		if ((millis()-holdMillisDraw) > matrix.outputFPS)
		{
			//if (FeedPlayModeA == 1 && FeedPlayModeB == 1) mixFeeds = true; //if both feeds are paused, still need to mix them for the output
			//If any media content has changed and the output FPS timer has elapsed, indicate to the transmission thread to send a packet
			if (mixFeeds == true)
			{
				//println("mixed "+(millis()-holdMillisDraw)+"    OutFPS: "+matrix.outputFPS);
				//now mix the layers
				MainMixFunction(); //mix FeedA and FeedB
				//transmitPixelBuffer = MixedContentGBuf.get();
				mixFeeds = false;
				holdMillisDraw = millis(); //update here
				//PacketReadyForTransmit = true; //indicates to transmission thread that a new frame is ready
				//delay(10); //should sleep thread - does reduce CPU usage it seems
			} //end mixFeeds if()
		}
        } //end while - loops
    }//end thread
}
