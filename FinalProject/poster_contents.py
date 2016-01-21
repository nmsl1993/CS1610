#! /usr/bin/env python2
import numpy as np
import caffe
import sys
import nltk

image_path = sys.argv[1]
caffe_root = '/home/noah/Development/caffe/'
caffe.set_mode_cpu()
imagenet_labels_filename = caffe_root + 'data/ilsvrc12/synset_words.txt'
labels = np.loadtxt(imagenet_labels_filename, str, delimiter='\t')
short_labels = np.asarray([s.split(' ')[1].rstrip(',') for s in labels])
net = caffe.Net(caffe_root + 'models/bvlc_reference_caffenet/deploy.prototxt',
              caffe_root + 'models/bvlc_reference_caffenet/bvlc_reference_caffenet.caffemodel',
                caffe.TEST)

# input preprocessing: 'data' is the name of the input blob == net.inputs[0]
transformer = caffe.io.Transformer({'data': net.blobs['data'].data.shape})
transformer.set_transpose('data', (2,0,1))
transformer.set_mean('data', np.load(caffe_root + 'python/caffe/imagenet/ilsvrc_2012_mean.npy').mean(1).mean(1)) # mean pixel
transformer.set_raw_scale('data', 255)  # the reference model operates on images in [0,255] range instead of [0,1]
transformer.set_channel_swap('data', (2,1,0))  # the reference model has channels in BGR order instead of RGB

#set net batch size of 50

net.blobs['data'].reshape(50,3,227,227)
net.blobs['data'].data[...] = transformer.preprocess('data', caffe.io.load_image(image_path))

out = net.forward()
top_k = net.blobs['prob'].data[0].flatten().argsort()[-1:-20:-1]
print labels[top_k]

