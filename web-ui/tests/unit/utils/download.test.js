import {getFilename, triggerBlobDownload} from '@/utils/download';

describe('getFilename()', () => {
  it('should parse a plain filename', () => {
    const filename = 'report.geojson';
    expect(getFilename(`attachment; filename=${filename}`)).toBe(filename);
  });

  it('should parse a quoted filename', () => {
    const filename = 'report.geojson';
    expect(getFilename(`attachment; filename="${filename}"`)).toBe(filename);
  });

  it('should trim whitespace around the filename', () => {
    const filename = 'report.geojson';
    expect(getFilename(`attachment; filename=  ${filename}  `)).toBe(filename);
  });

  it('should return null when content-disposition is null', () => {
    expect(getFilename(null)).toBeNull();
  });

  it('should return null when content-disposition is undefined', () => {
    expect(getFilename(undefined)).toBeNull();
  });

  it('should return null when content-disposition has no filename', () => {
    expect(getFilename('attachment')).toBeNull();
  });

  it('should return null when content-disposition is an empty string', () => {
    expect(getFilename('')).toBeNull();
  });
});

describe('triggerBlobDownload()', () => {
  const mockUrl = 'blob:mock-url';
  let createObjectURLMock;
  let revokeObjectURLMock;
  let anchorMock;

  beforeEach(() => {
    createObjectURLMock = jest.fn().mockReturnValue(mockUrl);
    revokeObjectURLMock = jest.fn();
    window.URL.createObjectURL = createObjectURLMock;
    window.URL.revokeObjectURL = revokeObjectURLMock;

    anchorMock = {
      click: jest.fn(),
      remove: jest.fn(),
      href: '',
      download: '',
    };

    jest.spyOn(document, 'createElement').mockReturnValue(anchorMock);
    jest.spyOn(document.body, 'appendChild').mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('should create an object URL from the blob', () => {
    const blob = new Blob(['{}'], {type: 'application/geo+json'});
    triggerBlobDownload(blob, 'export.geojson');

    expect(createObjectURLMock).toHaveBeenCalledWith(blob);
  });

  it('should set href and download on the anchor', () => {
    const filename = 'export.geojson';
    const blob = new Blob(['{}'], {type: 'application/geo+json'});
    triggerBlobDownload(blob, filename);

    expect(anchorMock.href).toEqual(mockUrl);
    expect(anchorMock.download).toBe(filename);
  });

  it('should click and remove the anchor', () => {
    triggerBlobDownload(new Blob(), 'file.geojson');

    expect(anchorMock.click).toHaveBeenCalled();
    expect(anchorMock.remove).toHaveBeenCalled();
  });

  it('should revoke the object URL after download', () => {
    triggerBlobDownload(new Blob(), 'file.geojson');

    expect(revokeObjectURLMock).toHaveBeenCalledWith(mockUrl);
  });
});
