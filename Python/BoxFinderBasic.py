#! /home/pi/.virtualenvs/cv/bin/python
import numpy as np
import cv2

import socket

UDP_IP = "10.35.1.2"
#UDP_IP = "10.17.48.24"
UDP_PORT = 1025
#CHANGE TO ALLOWED VALUES
#CHANGE TO ALLOWED VALUES
#CHANGE TO ALLOWED VALUES
#CHANGE TO ALLOWED VALUES

cap = cv2.VideoCapture(0)
sizeX = 150
sizeY = 120

#color range
lower_h = 25
lower_s = 63
lower_v = 120

upper_h = 50
upper_s = 255
upper_v = 255

#define the color range
#h is in the range (0,180)
#s is in the range (0,255)
#h is in the range (0,255)
lower_yellow = np.array([lower_h,lower_s,lower_v])
upper_yellow = np.array([upper_h,upper_s,upper_v])

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
logF = open("/home/pi/Desktop/BoxFinderBasic.log", "a")
logF.write("Starting robot camera main loop\n")
logF.flush()

while(True):
    #read from the camera feed
    ret, frame = cap.read()

    #if there is no camera feed, do not move on to the rest of the code
    if(np.size(frame,0) == 0):
        print("Frame is 0! Try reconnecting the camera port")
        continue

    #resize image
    frame = cv2.resize(frame, (sizeX,sizeY))

    #convert color to HSV
    hsv = cv2.cvtColor(frame,cv2.COLOR_BGR2HSV)

    #threshold to see where the wanted color in the image is
    mask = cv2.inRange(hsv,lower_yellow,upper_yellow)

    #make the image black and white
    res = cv2.bitwise_and(frame,frame,mask = mask)

    #blur the image
    blurred = cv2.GaussianBlur(mask,(7,7),0)

    #threshold the image to black/white; anything that is blurred is now cut out
    ret,threshold = cv2.threshold(blurred,254,255,cv2.THRESH_BINARY)

    #find contours around the box
    contours = cv2.findContours(threshold, 1, 2)

    
    #for (MatOfPoint contour : contours):
    cnt = contours[0]
    M = cv2.moments(cnt)

    #find the average value
    if(M['m00'] == 0):
        continue
    x = int(M['m10']/M['m00'])
    
    x = x - 75
    
    if(abs(x)<3):
        x = 0

    #obligatory print statement
    print(x)
    logF.write(str(x)+"\n")
    logF.flush()

    #sends packets to the UDP ip and port
    sock.sendto(str(x).encode('utf-8'), (UDP_IP, UDP_PORT))

    #uncomment these lines to show the actual video feed
    #cv2.imshow('frame',contours)
    #if cv2.waitKey(1) & 0xFF == ord('q'):
    #    break
cap.release()
cv2.destroyAllWindows()
logF.close()
