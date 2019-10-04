import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.awt.Color;


public class q1 {

    // Number of threads to use
    public static int threads = 1;
    private static Color[][] imgBuffer;
    public static int kernel[][] = {{-1, -1, -1}, {-1, 8, -1}, {-1, -1, -1}};
    public static BufferedImage outputimage;
    public static Color[][] getBuffer() {
    	return imgBuffer;
    }
    public static void setBuffer(Color color, int x, int y) {
    	q1.imgBuffer[x][y] = color;
 
    }
    
    public static void main(String[] args) {
        try {
            if (args.length>0) {
                threads = Integer.parseInt(args[0]);
            }

            // read in an image from a file
            BufferedImage img = ImageIO.read(new File("image.jpg"));
            // store the dimensions locally for convenience
            int width  = img.getWidth();
            int height = img.getHeight();

            // create an output image
            outputimage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
            q1.imgBuffer = new Color[width][height];
            // ------------------------------------
            // Your code would go here
            long start_time = System.currentTimeMillis();
            Color[][] color = new Color[width][height];
            
            //store the each pixel in a 2d array
            for(int i = 0; i < width; i++) {
            	for(int j = 0; j < height; j++) {
            		color[i][j] = new Color(img.getRGB(i, j));
            	}
            }
            
            //Calculate how many rows that each thread will be modifying
            Thread[] thread_pool = new Thread[threads];
            int thread_size = (int)Math.ceil((double)width/(double)threads);
            int index = 0;
            
            //Add threads into a pool to start
            
            	for(int start = 0; start < width; start += thread_size) {
            		int end = Math.min(start + thread_size - 1, width - 1);
            		thread_pool[index] = new Thread(new Convolution(start, end, color, height, width));
            		index++;
            	}
            
            
            //Start the threads
            for(int i = 0; i < thread_pool.length; i++) {
            	thread_pool[i].start();
            }
            
            //Wait until all threads finish their work in java. https://stackoverflow.com/questions/7939257/wait-until-all-threads-finish-their-work-in-java
            for (Thread thread : thread_pool) {
            	try {
            		thread.join();
            	}
            	catch(InterruptedException e){
            		System.out.println("error + " + e);
            	}
            }
            
            
            // The easiest mechanisms for getting and setting pixels are the
            // BufferedImage.setRGB(x,y,value) and getRGB(x,y) functions.
            // Note that setRGB is synchronized (on the BufferedImage object).
            // Consult the javadocs for other methods.

            // The getRGB/setRGB functions return/expect the pixel value in ARGB format, one byte per channel.  For example,
            //  int p = img.getRGB(x,y);
            // With the 32-bit pixel value you can extract individual colour channels by shifting and masking:
            //  int red = ((p>>16)&0xff);
            //  int green = ((p>>8)&0xff);
            //  int blue = (p&0xff);
            // If you want the alpha channel value it's stored in the uppermost 8 bits of the 32-bit pixel value
            //  int alpha = ((p>>24)&0xff);
            // Note that an alpha of 0 is transparent, and an alpha of 0xff is fully opaque.
            
            //replace the output data with the buffer data
            
            //System.out.println(q1.getBuffer()[3967][0].getRGB());
            /*
            for(int x = 0; x < width; x ++) {
            	//System.out.print(x + " ");
            	for(int y = 0; y < height; y++) {
            		//System.out.println(y);
            		outputimage.setRGB(x, y,q1.getBuffer()[x][y].getRGB());
            	}
            }
            */
            long end_time = System.currentTimeMillis();
            System.out.println("total time is " + (end_time - start_time));
            // ------------------------------------
            
            // Write out the image
            File outputfile = new File("outputimage.png");
            ImageIO.write(outputimage, "png", outputfile);
            

        } catch (Exception e) {
            System.out.println("ERROR " +e);
            e.printStackTrace();
        }
    }


}
class Convolution implements Runnable{
    private int start;
    private int end;
    private Color[][] image;
    private int height;
    private int width;
    
    public Convolution(int start, int end, Color[][] img, int height, int width){
    	this.start = start;
    	this.end = end;
    	this.image = img;
    	this.height = height;
    	this.width = width;
    }

    //Only one thread can be accessing the buffer image at the same time
    public synchronized void setBuffer(Color color, int x, int y) {
    	q1.setBuffer(color, x, y);
    }
	@Override
	public void run() {
		// TODO Auto-generated method stub
		for(int x = start; x <= end; x++) {
			//System.out.println(x);
			for(int y = 0; y < height; y++) {
				//set rgb accumulators to zero
				int red_acc = 0;
				int green_acc = 0;
				int blue_acc = 0;
				
				//for each kernel row and for each value in the row
				for(int i = 0; i < 3; i++) {
					for(int j = 0; j < 3; j++) {
						//check if the pixel offset is within the dimensional bounds
						if((x - 1 >= 0) && (y - 1 >=0)) {
							if((x + 1 < width) && (y + 1 < height)) {
								//multiply the pixel colour value by the corresponding kernel value
		                        //add result to accumulator
								red_acc += q1.kernel[i][j] * this.image[x + i - 1][y + j - 1].getRed();
								green_acc += q1.kernel[i][j] * this.image[x + i - 1][y + j - 1].getGreen();
								blue_acc += q1.kernel[i][j] * this.image[x + i - 1][y + j - 1].getBlue();
							}
						}
					}
				}
				
				//Constrain rgb values to be between 0 and 255
				if (red_acc > 255) {
					red_acc = 255;
				}
				if(red_acc < 0) {
					red_acc = 0;
				}
				if (green_acc > 255) {
					green_acc = 255;
				}
				if(green_acc < 0) {
					green_acc = 0;
				}
				if (blue_acc > 255) {
					blue_acc = 255;
				}
				if(blue_acc < 0) {
					blue_acc = 0;
				}
				
				//Set the global imgBuffer value
				
				setBuffer(new Color(red_acc, green_acc, blue_acc), x, y);
				q1.outputimage.setRGB(x, y, q1.getBuffer()[x][y].getRGB());
				
			}
		}
		
	}
	
}
