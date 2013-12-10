#!/usr/bin/python

import sys
import math
import numpy as np

from pylab import *
from random import *


def make_fig(dist_class, dist=None, mu = 0., sig = 1., xmin = -5., xmax = 5., fig_num=0, xlab=None, ylab=None, tit=None, tit_fontsz=30):
    if not dist: dist = dist_class(mu, sig)
    Xs = np.linspace(xmin, xmax, 100)
    Ys = [dist.eval(X) for X in Xs]
    figure(fig_num)
    plot(Xs, Ys, '-')
    if xlab: xlabel(xlab, fontsize=24)
    if ylab: ylabel(ylab, fontsize=24)
    if tit: title(tit, fontsize=tit_fontsz)

"""
 math.erf() is standard for Python 2.7+, but 
 the Andrew machines use Python 2.6.6.

 Consequently I have taken this implementation
 of erf() from the web.
"""
def erf(x):
    sign = 1 if x >= 0 else -1
    x = abs(x)

    # constants
    a1 =  0.254829592
    a2 = -0.284496736
    a3 =  1.421413741
    a4 = -1.453152027
    a5 =  1.061405429
    p  =  0.3275911

    # A&S formula 7.1.26
    t = 1.0/(1.0 + p*x)
    y = 1.0 - (((((a5*t + a4)*t) + a3)*t + a2)*t + a1)*t*math.exp(-x*x)
    return sign*y # erf(-x) = -erf(x)

class norm_pdf_dist:

    def __init__(self, mu, sig):
        self.mu = mu
        self.sig = sig

    def eval(self, x):
        return (1./(2*math.pi*self.sig**2)**.5) * math.exp(-(x - self.mu)**2/(2*self.sig**2))

class norm_cdf_dist:

    def __init__(self, mu, sig):
        self.mu = mu
        self.sig = sig

    def eval(self, x):
        return .5 * (1 + erf((x - self.mu) / ((2 * self.sig**2)**.5)))

class g_dist:

    def __init__(self, mu, sig):
        self.mu = mu
        self.sig = sig
        self.phi = norm_pdf_dist(0., 1.)
        PHI = norm_cdf_dist(0., 1.)
        self.denom = PHI.eval((1. - self.mu)/self.sig) - PHI.eval(-self.mu/self.sig)

    def eval(self, x):
        return (1./self.sig) * self.phi.eval((x - self.mu)/self.sig) / self.denom

class p_dist:

    def __init__(self, mu_1, mu_2, sig_1, sig_2):
        self.g1 = g_dist(mu_1, sig_1)
        self.g2 = g_dist(mu_2, sig_2)

    def eval(self, x):
        return .5 * (self.g1.eval(x) + self.g2.eval(x))

def rejection_sample(xmin, xmax, pdf, count):
    results = []
    bns = np.linspace(xmin, xmax, 1000)
    fn_max = max([pdf(bns[i]) for i in range(len(bns))])
    while (len(results) < count):
        x = uniform(xmin, xmax)
        h = uniform(0, fn_max)
        if (h < pdf(x)):
            results.append(x)
    return results

def make_2D_data(num_points, mus=[.3, .7, .3, .7], sigs=[.03, .03, .01, .01], rot=None):
    mu_1x, mu_2x, mu_1y, mu_2y = mus
    sig_1x, sig_2x, sig_1y, sig_2y = sigs
    p_x = p_dist(mu_1x, mu_2x, sig_1x, sig_2x)
    p_y = p_dist(mu_1y, mu_2y, sig_1y, sig_2y)
    make_fig(p_dist, dist=p_x, xmin=0, xmax=1)
    make_fig(p_dist, dist=p_y, xmin=0, xmax=1)
    xs = rejection_sample(0., 1., p_x.eval, num_points)
    ys = rejection_sample(0., 1., p_y.eval, num_points)
    return np.column_stack((np.array(xs), np.array(ys)))

def make_DNA_data(num_strands, strand_length):
    return np.array([[randint(1, 4) for b in range(strand_length)] for n in range(num_strands)])

if __name__ == "__main__":

    data = make_2D_data(5000)
    #np.savetxt('2D_data.txt', data, fmt='%10.5f', delimiter='\t')

    xs, ys = data[:,0], data[:,1]
    figure(42)
    plot(xs, ys, '.')
    #plot([.293, .733, .232, .786], [.763, .239, .293, .738], 'x', color='r', markersize=10)
    plot([.699, .299, .300, .699], [.699, .7, .3, .3], 'x', color='r', markersize=10)

    print 'number of 2D data points:', len(data)

    data = make_DNA_data(5000, 10)
    #np.savetxt('DNA_data.txt', data, fmt='%i')
    #np.savetxt('DNA_data.txt', data, fmt='%10.5f', delimiter='\t')

    data = make_DNA_data(5000, 2)
    np.savetxt('DNA_data_2D.txt', data, fmt='%10.5f', delimiter='\t')

    print 'length of DNA strand:', len(data[0])
    print 'number of DNA strands:', len(data)

    show()
