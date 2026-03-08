import { useEffect, useMemo, useState } from 'react';

type UseAdaptiveTableOptions = {
  desktopReservedHeight?: number;
  mobileReservedHeight?: number;
  rowHeight?: number;
  minRowsDesktop?: number;
  minRowsMobile?: number;
  maxRows?: number;
  mobileBreakpoint?: number;
};

const clamp = (value: number, min: number, max: number) => Math.max(min, Math.min(value, max));

const useAdaptiveTable = (options?: UseAdaptiveTableOptions) => {
  const {
    desktopReservedHeight = 320,
    mobileReservedHeight = 360,
    rowHeight = 54,
    minRowsDesktop = 8,
    minRowsMobile = 5,
    maxRows = 30,
    mobileBreakpoint = 768,
  } = options || {};

  const [isMobile, setIsMobile] = useState(false);
  const [pageSize, setPageSize] = useState(10);
  const [scrollY, setScrollY] = useState(420);

  useEffect(() => {
    const compute = () => {
      const mobile = window.innerWidth < mobileBreakpoint;
      const reservedHeight = mobile ? mobileReservedHeight : desktopReservedHeight;
      const minRows = mobile ? minRowsMobile : minRowsDesktop;

      const available = Math.max(window.innerHeight - reservedHeight, rowHeight * minRows);
      const rows = clamp(Math.floor(available / rowHeight), minRows, maxRows);
      setIsMobile(mobile);
      setPageSize(rows);
      setScrollY(rows * rowHeight + 2);
    };

    compute();
    window.addEventListener('resize', compute);
    return () => window.removeEventListener('resize', compute);
  }, [
    desktopReservedHeight,
    maxRows,
    minRowsDesktop,
    minRowsMobile,
    mobileBreakpoint,
    mobileReservedHeight,
    rowHeight,
  ]);

  return useMemo(
    () => ({
      isMobile,
      pageSize,
      scrollY,
    }),
    [isMobile, pageSize, scrollY],
  );
};

export default useAdaptiveTable;
