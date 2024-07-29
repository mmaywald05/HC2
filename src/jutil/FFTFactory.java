package jutil;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FFTFactory {
    public static Complex[] loadSamplesAsComplex (String filePath){
        try {
            float[] samples = WavFileFactory.readWavFile(filePath);

            Complex[] complex = new Complex[samples.length];
            for (int i = 0; i < samples.length; i++) {
                complex[i] = new Complex(samples[i], 0);
            }
            return complex;
        } catch (UnsupportedAudioFileException | IOException e) {
            System.out.println("Error reading file`?");
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] readAllBytes(AudioInputStream audioInputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;

        while ((bytesRead = audioInputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        return buffer.toByteArray();
    }




    public static void DFT_SEQ(String filePath, int blockSize, int shift, double threshold) {
        Complex[] input = loadSamplesAsComplex(filePath);
        int N = input.length;
        float[] magnitudes = new float[blockSize];
        int numBlocks = (N - blockSize) / shift + 1 ;



        blockwise_DFT(input, magnitudes, blockSize, shift, numBlocks);
        average(magnitudes, numBlocks);
        normalizeMagnitudes(magnitudes);


        try {
            WavFileFactory.writeFloatArrayToFile(magnitudes,"CPU_sequentiell.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < magnitudes.length; i++) {
            System.out.println("Bin: " + i + " Magnitude = " + (magnitudes[i]));
        }
    }

    private static void DFT(Complex[] input, float[] magnitudes, int N, int k, int s, int numBlocks){
        for (int i= 0; i < input.length; i++) {
            Complex number = new Complex(0, 0);
            for (int j = 0; j < input.length; j++) {
                double angle = 2 * Math.PI * i * j / input.length;
                Complex w = new Complex((float) Math.cos(angle), (float) -Math.sin(angle));
                Complex product = Complex.multiply(input[j], w);
                number = Complex.add(number, product);
            }
            float mag = Complex.magnitude(number);
            magnitudes[i] += mag;
        }
    }

    private static void blockwise_DFT(Complex[] input, float[] magnitudes, int k, int s, int numBlocks){

        for (int blockId = 0; blockId < numBlocks; blockId++) {
            int startIndex = blockId * s;
            int endIndex = startIndex + k;


            for (int i = startIndex; i < endIndex; i++) {
                Complex number = new Complex(0f, 0f);
                for (int j = startIndex; j < endIndex; j++) {
                    double angle = 2 * Math.PI * (i-startIndex) * (j-startIndex) / k;
                    Complex w = new Complex(Math.cos(angle), -Math.sin(angle));
                    w = Complex.multiply(input[j], w);
                    number.add(w);
                }
                float mag = Complex.magnitude(number);
                magnitudes[i-startIndex] += mag;
            }
        }
    }

    public static void DFT_PAR(String filePath, int blockSize, int shift, double threshold) throws ExecutionException, InterruptedException {
        Complex[] input = loadSamplesAsComplex(filePath);
        int N = input.length;
        int numBlocks = (N - blockSize) / shift +1;

        int numCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numCores);
        List<Future<float[]>> futures = new ArrayList<>();
        float[] magnitudes = new float[blockSize];


        //Complex[] windowed_input = hann_window(input);


        for (int blockIndex = 0; blockIndex < numBlocks; blockIndex++) {
            final int startIndex = blockIndex * shift;
            futures.add(
                    executor.submit(() -> {
                        Complex[] block = new Complex[blockSize];
                        System.arraycopy(input, startIndex, block, 0, blockSize);
                        return parallel_blockwise_dft_instance(block, numBlocks, startIndex, startIndex+blockSize);
                    }));
        }
        // auf cores warten
        System.out.println("Waiting for futures");
        for (Future<float[]> future : futures) {
            float[] localMag = future.get();
            for (int i = 0; i < magnitudes.length; i++) {
                magnitudes[i] = localMag[i];
            }
        }
        // durchschnitt
        average(magnitudes, numBlocks);
        // Normalisieren
        normalizeMagnitudes(magnitudes);
        // ausgabe
        try {
            WavFileFactory.writeFloatArrayToFile( magnitudes,"CPU_parallel.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < magnitudes.length; i++) {
            System.out.println("Bin: " + i + " Magnitude = " + (magnitudes[i]));
        }
    }


    public static Complex[] hann_windpw(Complex[] input){
        Complex[] windowed_input = new Complex[input.length];
        float w = 0;
        float windowedSample;


        for (int i = 0; i < input.length; i++) {
            w = (float) (1 - Math.cos(2* Math.PI * i / input.length));
            windowedSample = w * input[i].x;
            windowed_input[i] = new Complex(windowedSample, 0);

        }


        return windowed_input;
    }


    public static float[] parallel_blockwise_dft_instance(Complex[] input, int numBlocks, int startIndex, int end) {
        float[] magnitudes  = new float[input.length];
        int N = input.length;
        for (int i = 0; i < N; i++) {
            Complex number = new Complex(0f,0f);
            for (int j = 0; j < N; j++) {
                double angle = 2 * Math.PI * i * j / N;
                Complex w = new Complex(Math.cos(angle), -Math.sin(angle));
                w = Complex.multiply(input[j], w);
                number.add(w);
            }
            float mag = Complex.magnitude(number) / numBlocks;
            magnitudes[i] += mag;
        }
        return magnitudes;
    }

    public static void dft(Complex[] input, float[] magnitudes){
        int N = input.length;
        for (int i = 0; i < N; i++) {
            Complex number = new Complex(0f,0f);
            for (int j = 0; j < N; j++) {
                double angle = 2 * Math.PI * i * j / N;
                Complex w = new Complex(Math.cos(angle), -Math.sin(angle));
                w = Complex.multiply(input[j], w);
                number.add(w);
            }
            float mag = Complex.magnitude(number);
            magnitudes[i] += mag;
        }
    }

    public static void average(float[] magnitudes, int numBlocks){
        for (int i = 0; i < magnitudes.length; i++) {
            magnitudes[i] = magnitudes[i] / numBlocks;
        }
    }


    public static void normalizeMagnitudes(float[] magnitudes){
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;

        for (int i = 0; i < magnitudes.length; i++) {
            if(magnitudes[i] < min)
                min = magnitudes[i];
            if(magnitudes[i] > max)
                max = magnitudes[i];
        }
        for (int i = 0; i < magnitudes.length; i++) {
            magnitudes[i] = (magnitudes[i]-min)/(max-min);
        }
    }

    public static class Complex {
        float x;
        float y;

        public Complex (){
            this.x = 0;
            this.y = 0;
        }
        public Complex(float x, float y){
            this.x = x;
            this.y = y;
        }
        public Complex(double x, double y){
            this.x = (float) x;
            this.y = (float) y;
        }

        public float x(){ return this.x;}
        public float y(){return this.y;}

        public static Complex add(Complex a, Complex b){
            return new Complex(a.x + b.x, a.y + b.y);
        }
        public void add(Complex c){
            this.x += c.x;
            this.y += c.y;
        }

        public static Complex multiply(Complex a, Complex b){
            return new Complex(a.x * b.x - a.y * b.y, a.x * b.y + a.y * b.x);
        }
        public static float magnitude(Complex a){
            return (float) Math.sqrt(a.x * a.x + a.y * a.y);
        }

        @Override
        public String toString() {
            return x+"+i"+y;
        }
    }
}