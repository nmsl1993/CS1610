
[x, fs] = audioread('data/mario_cut.wav');
fs
samples = length(x)
x = x(:,1);
old_pow = sum(x.^2) 
xspectrum = fft(x);
[x_spectsort, x_spectsort_idx]  = sort(abs(xspectrum));

xspectrum(x_spectsort_idx(1:end-2001)) = 0;
%xspectrum(x_spectsort_idx(1000:end))=0;
y = ifft(xspectrum);
new_pow = sum(y.^2)
new_max = max(y);
y = y.*(1*sqrt(old_pow/new_pow));
new_pow = sum(y.^2)

audiowrite('out.wav',y,fs)

