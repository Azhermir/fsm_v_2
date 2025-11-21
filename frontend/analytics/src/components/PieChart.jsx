import { useEffect, useRef } from 'react'
import './PieChart.css'

const PieChart = ({ data }) => {
  const canvasRef = useRef(null)

  useEffect(() => {
    if (!canvasRef.current || !data || data.length === 0) return

    const canvas = canvasRef.current
    const ctx = canvas.getContext('2d')
    if (!ctx) return // Guard against null context in tests
    
    const centerX = canvas.width / 2
    const centerY = canvas.height / 2
    const radius = Math.min(centerX, centerY) - 10

    // Clear canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    // Calculate total
    const total = data.reduce((sum, item) => sum + item.value, 0)
    
    if (total === 0) {
      // Draw empty state
      ctx.fillStyle = '#666'
      ctx.font = '14px sans-serif'
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.fillText('No data available', centerX, centerY)
      return
    }

    // Draw pie slices
    let currentAngle = -Math.PI / 2 // Start from top

    data.forEach((item) => {
      const sliceAngle = (item.value / total) * 2 * Math.PI

      // Draw slice
      ctx.beginPath()
      ctx.moveTo(centerX, centerY)
      ctx.arc(centerX, centerY, radius, currentAngle, currentAngle + sliceAngle)
      ctx.closePath()
      ctx.fillStyle = item.color
      ctx.fill()
      ctx.strokeStyle = '#fff'
      ctx.lineWidth = 2
      ctx.stroke()

      // Draw label if slice is large enough
      if (sliceAngle > 0.1) {
        const labelAngle = currentAngle + sliceAngle / 2
        const labelX = centerX + (radius * 0.7) * Math.cos(labelAngle)
        const labelY = centerY + (radius * 0.7) * Math.sin(labelAngle)

        ctx.fillStyle = '#fff'
        ctx.font = 'bold 12px sans-serif'
        ctx.textAlign = 'center'
        ctx.textBaseline = 'middle'
        ctx.fillText(item.value.toString(), labelX, labelY)
      }

      currentAngle += sliceAngle
    })
  }, [data])

  return (
    <div className="pie-chart-container">
      <canvas 
        ref={canvasRef} 
        width={300} 
        height={300}
        aria-label="Priority distribution pie chart"
      />
      <div className="pie-chart-legend">
        {data && data.map((item, index) => (
          <div key={index} className="legend-item">
            <span 
              className="legend-color" 
              style={{ backgroundColor: item.color }}
            />
            <span className="legend-label">{item.label}: {item.value}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

export default PieChart
