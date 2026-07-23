/*
* Copyright (c) 2009-2022. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
