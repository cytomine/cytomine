import hamamatsuImage from '@/assets/brands/hamamatsu.jpg';
import threedhImage from '@/assets/brands/3dh.png';
import aperioImage from '@/assets/brands/aperio.jpg';
import leicaImage from '@/assets/brands/leica.png';
import rocheImage from '@/assets/brands/roche.gif';
import philipsImage from '@/assets/brands/philips.svg';

let vendors = {
  hamamatsu: {
    imgPath: hamamatsuImage,
    name: 'Hamamatsu Photonics'
  },
  '3dh': {
    imgPath: threedhImage,
    name: '3DHISTECH Ltd.'
  },
  aperio: {
    imgPath: aperioImage,
    name: 'Aperio'
  },
  leica: {
    imgPath: leicaImage,
    name: 'Leica Biosystems'
  },
  roche: {
    imgPath: rocheImage,
    name: 'La Roche Ltd.'
  },
  philips: {
    imgPath: philipsImage,
    name: 'Philips'
  }
};

export default function vendorFromFormat(format) {
  switch (format) {
    case 'NDPI':
    case 'VMS':
      return vendors['hamamatsu'];
    case 'MRXS':
      return vendors['3dh'];
    case 'SVS':
      return vendors['aperio'];
    case 'SCN':
      return vendors['leica'];
    case 'VENTANA':
    case 'BIF':
      return vendors['roche'];
    case 'PHILIPS':
      return vendors['philips'];
    default:
      return null;
  }
}
